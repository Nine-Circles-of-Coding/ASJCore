package shadersmod.client;

import org.lwjgl.opengl.GL11;

public class Shaders {
	
	public static boolean shaderPackLoaded = false;
	
	public static void sglFogi(int pname, int param) {
		GL11.glFogi(pname, param);
	}
}
