package codechicken.multipart.minecraft;

abstract class McMetaPart extends McBlockPart {
	public byte meta;
}

public class ButtonPart extends McMetaPart {
	public static int[] metaSideMap, sideMetaMap;
	public ButtonPart(int meta) {}
	public boolean pressed() {
		return false;
	}
}