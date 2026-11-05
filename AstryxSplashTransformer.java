
package com.astryx.client.core.splash;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class AstryxSplashTransformer implements IClassTransformer {

    private static final String TARGET_CLASS =
            "net.minecraftforge.fml.client.SplashProgress$3";

    private static final String DRAW_BAR_DESCRIPTOR =
            "(Lnet/minecraftforge/fml/common/ProgressManager$ProgressBar;)V";

    private static final String RENDERER_CLASS =
            "com/astryx/client/core/splash/AstryxSplashRenderer";

    private static final String PROGRESS_BAR_CLASS =
            "net/minecraftforge/fml/common/ProgressManager$ProgressBar";

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass
    ) {
        if (basicClass == null) {
            return null;
        }

        if (!TARGET_CLASS.equals(transformedName)
                && !TARGET_CLASS.equals(name)) {
            return basicClass;
        }

        try {
            ClassNode classNode = new ClassNode();

            ClassReader reader = new ClassReader(basicClass);
            reader.accept(classNode, 0);

            boolean modified = false;

            for (MethodNode method : classNode.methods) {

                // Replace Forge's standard progress bar renderer.
                if ("drawBar".equals(method.name)
                        && DRAW_BAR_DESCRIPTOR.equals(method.desc)) {

                    method.instructions.clear();
                    method.tryCatchBlocks.clear();

                    if (method.localVariables != null) {
                        method.localVariables.clear();
                    }

                    InsnList instructions = new InsnList();

                    instructions.add(new VarInsnNode(
                            Opcodes.ALOAD,
                            0
                    ));

                    instructions.add(new VarInsnNode(
                            Opcodes.ALOAD,
                            1
                    ));

                    instructions.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            RENDERER_CLASS,
                            "render",
                            "(Ljava/lang/Object;L"
                                    + PROGRESS_BAR_CLASS
                                    + ";)V",
                            false
                    ));

                    instructions.add(new InsnNode(
                            Opcodes.RETURN
                    ));

                    method.instructions.add(instructions);

                    method.maxStack = 2;
                    method.maxLocals = 2;

                    modified = true;
                }

                // Keep only the first progress bar in Forge's render loop.
                if ("run".equals(method.name)
                        && "()V".equals(method.desc)) {

                    int drawBarCalls = 0;

                    for (AbstractInsnNode instruction =
                         method.instructions.getFirst();
                         instruction != null;) {

                        AbstractInsnNode next =
                                instruction.getNext();

                        if (instruction instanceof MethodInsnNode) {

                            MethodInsnNode invocation =
                                    (MethodInsnNode) instruction;

                            boolean isDrawBar =
                                    invocation.getOpcode()
                                            == Opcodes.INVOKESPECIAL
                                            && TARGET_CLASS.equals(
                                            invocation.owner
                                    )
                                            && "drawBar".equals(
                                            invocation.name
                                    )
                                            && DRAW_BAR_DESCRIPTOR.equals(
                                            invocation.desc
                                    );

                            if (isDrawBar) {

                                drawBarCalls++;

                                if (drawBarCalls > 1) {

                                    InsnList removal = new InsnList();

                                    // Remove the ProgressBar argument.
                                    removal.add(new InsnNode(
                                            Opcodes.POP
                                    ));

                                    // Remove the renderer instance.
                                    removal.add(new InsnNode(
                                            Opcodes.POP
                                    ));

                                    method.instructions.insertBefore(
                                            instruction,
                                            removal
                                    );

                                    method.instructions.remove(
                                            instruction
                                    );

                                    modified = true;
                                }
                            }
                        }

                        instruction = next;
                    }
                }
            }

            if (!modified) {
                System.err.println(
                        "[Astryx] Splash transformer found no matching methods."
                );

                return basicClass;
            }

            System.out.println(
                    "[Astryx] Successfully transformed Forge splash renderer."
            );

            ClassWriter writer = new ClassWriter(
                    ClassWriter.COMPUTE_MAXS
            );

            classNode.accept(writer);

            return writer.toByteArray();

        } catch (Throwable throwable) {

            System.err.println(
                    "[Astryx] Failed to transform Forge splash renderer."
            );

            throwable.printStackTrace();

            return basicClass;
        }
    }
}
