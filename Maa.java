

public class Maa {

	
	public static void main(String[] args) {
		String target1= "/home/lijq/workspace/QT/xintu.png";
		String saveFilePath1 = "/home/lijq/workspace/QT/qt"+".png";//待切割文件保存路径
		TileUtils tile = new TileUtils(target1, 3, 21, 20,12950279.8,4835948.98, saveFilePath1);
		try {
			tile.cutterAll();
			//tile.cutterOne(20);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
