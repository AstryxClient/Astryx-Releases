"""Reconstruct the tested Windows executable from its published predecessor."""
import bz2
import hashlib
from pathlib import Path
import sys

def number(data):
    magnitude = int.from_bytes(data, "little") & ((1 << 63) - 1)
    return -magnitude if data[-1] & 128 else magnitude

base, patch, dest = map(Path, sys.argv[1:])
old = base.read_bytes()
old_digest = hashlib.sha256(old).hexdigest()
expected = "b885e2c0af4d59e7f4946a00a9c7b0556a5c297d550727ed8509bb87a5f660f1"
if old_digest == expected:
    dest.write_bytes(old)
    sys.exit(0)
if old_digest != "be7e2762aa7cbbe5bfe99f0aff58b7cc2a27601a1ba571713cbd9cfa9bf7b7a4":
    sys.exit("Unexpected launcher baseline")
data = patch.read_bytes()
if hashlib.sha256(data).hexdigest() != "280fd7aa2af4cc95cafe0f1112436adfe4a6010b458dc6d666fbb64bffe60c51":
    sys.exit("Unexpected patch digest")
control_size, diff_size, length = (number(data[i:i+8]) for i in (8, 16, 24))
middle = 32 + control_size
end = middle + diff_size
control = bz2.decompress(data[32:middle])
diff = bz2.decompress(data[middle:end])
extra = bz2.decompress(data[end:])
output = bytearray()
pos = diff_pos = extra_pos = 0
for i in range(0, len(control), 24):
    copy, insert, seek = (number(control[j:j+8]) for j in (i, i+8, i+16))
    block = bytearray(diff[diff_pos:diff_pos+copy])
    for k in range(max(0,-pos), min(copy, len(old)-pos)):
        block[k] = (block[k] + old[pos+k]) & 255
    output.extend(block)
    output.extend(extra[extra_pos:extra_pos+insert])
    pos += copy+seek
    diff_pos += copy
    extra_pos += insert
if len(output) != length or hashlib.sha256(output).hexdigest() != expected:
    sys.exit("Reconstruction failed verification")
dest.write_bytes(output)
