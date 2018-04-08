
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;

import javax.imageio.ImageIO;

public class TileUtils {
	private int minLevel;
	private int maxLevel;
	private int picLevel;
	private double mercatorX;//摩卡坐标
	private double mercatorY;
	private String pic;
	private String savePath;

	public TileUtils(String pic, double mercatorX, double mercatorY,
			String savePath) {
		this.pic = pic;
		this.mercatorX = mercatorX;
		this.mercatorY = mercatorY;
		this.savePath = savePath;
	}

	public TileUtils(String pic, int minLevel, int maxLevel, int picLevel, double mercatorX,
			double mercatorY, String savePath) {
		this.pic = pic;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.mercatorX = mercatorX;
		this.mercatorY = mercatorY;
		this.savePath = savePath;
		this.picLevel = picLevel;
	}

	public void cutterAll() throws Exception {
		for (int i = minLevel; i <= maxLevel; i++) {
			cutterOne(i);
		}
	}

	public void cutterOne(int level) throws Exception {
		//图片中心的像素坐标(pixelX,pixelY)，图片中心的平面坐标即魔卡托坐标(mercatorX, mercatorY)
		//像素坐标  = 平面坐标 * Math.pow(2, level - 18)
		double pixelX = mercatorX * Math.pow(2, level - 18);
		double pixelY = mercatorY * Math.pow(2, level - 18);
		System.out.println("pixelX : " + pixelX);
		System.out.println("pixelY : " + pixelY);
		BufferedImage bi = ImageIO.read(new File(pic));
		int width = bi.getWidth();
		int height = bi.getHeight();
		//图片遵循原则：当前图片所属级别picLevel不缩放即像素级别相等。
		//按照公式缩放：当前级别图片长度 = 原图片长度 * Math.pow(2, level - picLevel)
		//minX: 图片左下角X坐标
		//minY: 图片左下角Y坐标
		//maxX: 图片右上角X坐标
		//maxY: 图片右上角Y坐标
		double minX = pixelX - width * Math.pow(2, level - picLevel) / 2;
		double minY = pixelY - height * Math.pow(2, level - picLevel) / 2;
		double maxX = pixelX + width  * Math.pow(2, level - picLevel) / 2;
		double maxY = pixelY + height * Math.pow(2, level - picLevel)  / 2;
		System.out.println("(minX,minY) = (" + minX + ", " + minY + ")" );
		System.out.println("(maxX,maxY) = (" + maxX + ", " + maxY + ")" );
		int neatMinX = (int) minX / 256;
		int remMinX = (int) minX % 256;
		int neatMinY = (int) minY / 256;
		int remMinY = (int) minY % 256 ;
		
		int neatMaxX = (int) maxX / 256;
		int remMaxX = 256 - (int) maxX % 256;
		int neatMaxY = (int) maxY / 256;
		int remMaxY = 256 - (int) maxY % 256;
		//(neatMinX,neatMinY)为图片左下角最近的整数图块坐标,neatMinX到neatMaxX即当前级别下切割图块的图块坐标x
		//(neatMaxX,neatMaxY)为图片右上角最近的整数图块坐标,neatMinY到neatMaxY即当前级别下切割图块的图块坐标y
		System.out.println("neatMinX: " + neatMinX);
		System.out.println("neatMaxX: " + neatMaxX);
		System.out.println("neatMinY: " + neatMinY);
		System.out.println("neatMaxY: " + neatMaxY);
		System.out.println("remMinX width remMaxX : " + remMinX + " "+ width + " "+ remMaxX );
		System.out.println("remMinY height remMaxY : " + remMinY + " " +  height +" " + remMaxY );
		
		// 扩充原图片为width * height --- > (remMinX + width + remMaxX ) * (remMinY +
		// height +remMaxY)
		int extendWidth = (neatMaxX - neatMinX + 1 ) * 256;
		int extendHeight = (neatMaxY - neatMinY + 1 ) * 256;
		System.out.println("extendWidth: " + extendWidth);
		System.out.println("extendHeight: " + extendHeight);
		
		BufferedImage outputImage = null;
		Graphics2D g = bi.createGraphics();
		BufferedImage extend = g.getDeviceConfiguration().createCompatibleImage(extendWidth, extendHeight, Transparency.TRANSLUCENT);
		g.dispose();
		g = extend.createGraphics();
		g.drawImage(extend, 0, 0, extendWidth, extendHeight, null);
		g.drawImage(bi, remMinX, remMaxY, (int) (width * Math.pow(2, level - picLevel)), (int)(height * Math.pow(2, level - picLevel)), null);
		outputImage = extend;
		
		//切割图片，共( neatMaxX - neatMinX + 1) * (neatMaxY - neatMinY + 1)份 256*256图片
		String dirName = savePath.substring(0, savePath.lastIndexOf("/")) + "/tiles/" + level;
		System.out.println("dirName : " + dirName);
		
		
		File dir = new File(dirName);
		Image image = extend.getScaledInstance(extendWidth, extendHeight, Image.SCALE_DEFAULT);
		if(dir.exists()) {
			System.out.println("创建目录失败！, 目录已存在！");
		} else {
			if(dir.mkdirs()) {
				ImageIO.write(extend, "png", new File(dirName + savePath.substring(savePath.lastIndexOf("/"))));
				System.out.println("savePath : " + dirName + savePath.substring(savePath.lastIndexOf("/")));
				System.out.println("Extend success!");
				int w = neatMaxX - neatMinX + 1;
				int h = neatMaxY - neatMinY + 1;
				for(int i = 0; i < w; i++) {
					for(int j = 1; j <= h; j++) {
						ImageFilter cropFilter = new CropImageFilter(256 * i, 256* (h - j), 256, 256);
						Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(),cropFilter));
						BufferedImage tag = new BufferedImage(256, 256 , BufferedImage.TYPE_INT_BGR);
						Graphics2D gs = tag.createGraphics();
						tag = gs.getDeviceConfiguration().createCompatibleImage(256, 256, Transparency.TRANSLUCENT);
						gs.dispose();
						gs = tag.createGraphics();
						gs.drawImage(img, 0, 0, null);
						g.dispose();
						String cropPicName = dirName + "/tile" + (neatMinX + i) + "_" + (neatMinY + j - 1) + ".png"; 
						ImageIO.write(tag, "png", new File(cropPicName));
					}
				}
				System.out.println("切割图片成功！");
			} else {
				System.out.println("创建目录失败！");
			}
		}
	}

}
