
package com.astryx.client.core.splash;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.Name("AstryxSplash")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.TransformerExclusions({
        "com.astryx.client.core.splash"
})
public class AstryxSplashLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                AstryxSplashTransformer.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
