import shadersmod.client.Shaders;

public class Config {
	
	public static float getFogStart() {
		return 0.75f;
	}
	
	public static boolean isClearWater() {
		return false;
	}
	
	public static boolean isDepthFog() {
		return true;
	}
	
	public static boolean isFogFancy() {
		return true;
	}
	
	public static boolean isFogFast() {
		return false;
	}
	
	public static boolean isShaders() {
		return Shaders.shaderPackLoaded;
	}
}
