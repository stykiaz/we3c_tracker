/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import setups.AppConfig;

/**
 * This class can create a heatmap from a given list of points.
 * The output will be a picture visualising the occurences of points.
 * <p>
 * CIRCLEPIC (an image of a circle, from black to transparent)
 * <p>
 * and
 * <p>
 * SPECTRUMPIC (a color gradiant
 *  where the color that represents "most" is at the bottom)
 * <p>
 * must be in the user.dir in:
 * <p>
 * heatmap/bolillaT.png
 * <p>
 * and
 * <p>
 * heatmap/colors.png
 *
 * @author sam
 */
public class HeatMap {
 /* // TODO memory leak? also, still quite slow. negate image really
 *    nececary? maybe it is possible to draw it correct from the start...
 *    also, maybe remember where circles where drawn, so on remap,
     * only those areas
 *    have to be mapped (disadvantage: more memory, also bad code, probably)
     */

    /** half of the size of the circle picture. */
    private static final int HALFCIRCLEPICSIZE = 32;
    /** path to picture of circle which gets more transparent to the outside. */
    private static final String CIRCLEPIC = AppConfig.appRootDirectory + "public/images/heatmap/bolillaT.png"; //System.getProperty("user.dir") + File.separator + "heatmap" + File.separator + "bolillaT.png";
    private static final String SPECTRUMPIC = AppConfig.appRootDirectory + "public/images/heatmap/colors.png"; //System.getProperty("user.dir")  + "/heatmap/colors.png";
    /** map to collect and sort points. */
    private Map<Integer, List<Point>> map;
    /** maximum occurance of the same coordinates. */
    private int maxOccurance = 1;
    /** maximal given x value. */
    private int maxXValue;
    /** maximal given y value. */
    private int maxYValue;
    /** name of file over which the heatmap will be laid. */
    private String lvlMap;
    /** name of file to save heatmap to. */
    private String outputFile;

    /**
     * constructs new instance of HeatMap from given list of points.
     * Depending on the amount of points, this may take a while,
     * as the points are being sorted.
     *
     * @param points the list of points
     * @param output name of file to store created heatmap in
     * @param lvlMap name of file to lay heatmap over
     */
    public HeatMap(List<Point> points, String output, String lvlMap) {
        outputFile = output;
        this.lvlMap = lvlMap;
        initMap(points);
    }

    /**
     * initiate map.
     * counts and sorts points and figures out max x and y values as well
     * as the maximal amount of points with same coordinates.
     * max x and y values will be used for the size of the heatmap.
     *
     * @param points list of points
     */
    private void initMap(List<Point> points) {
        map = new HashMap<Integer, List<Point>>();
        BufferedImage mapPic = loadImage(lvlMap);
        maxXValue = mapPic.getWidth();
        maxYValue = mapPic.getHeight();

        int pointSize = points.size();
        for (int i = 0; i < pointSize; i++) {
            Point point = points.get(i);
            // add point to right list.
            int hash = getkey(point);
            if (map.containsKey(hash)) {
                List<Point> thisList = map.get(hash);
                thisList.add(point);
                if (thisList.size() > maxOccurance) {
                    maxOccurance = thisList.size();
                }
                // if list did not exist, create new one and add point.
            } else {
                List<Point> newList = new LinkedList<Point>();
                newList.add(point);
                map.put(hash, newList);
            }
        }
    }

    /**
     * creates the heatmap.
     *
     * @param multiplier calculated opacity of every point will be
     * multiplied by this value. This leads to a HeatMap that is easier to read,
     * especially when there are not too many points or the points are too
     * spread out. Pass 1.0f for original.
     */
    public void createHeatMap(float multiplier) {

        BufferedImage circle = loadImage(CIRCLEPIC);
        BufferedImage heatMap = new BufferedImage(maxXValue, maxYValue, 6);
        paintInColor(heatMap, Color.white);

        Iterator<List<Point>> iterator = map.values().iterator();
        while (iterator.hasNext()) {
            List<Point> currentPoints = iterator.next();

            // calculate opaqueness
            // based on number of occurences of current point
            float opaque = currentPoints.size() / (float) maxOccurance;

            // adjust opacity so the heatmap is easier to read
            opaque = opaque * multiplier;
            if (opaque > 1) {
                opaque = 1;
            }

            Point currentPoint = currentPoints.get(0);

            // draw a circle which gets transparent from middle to outside
            // (which opaqueness is set to "opaque")
            // at the position specified by the center of the currentPoint
            addHeatSpot(heatMap, opaque, multiplier, currentPoint.x, currentPoint.y);
//            addImage(heatMap, circle, opaque, (currentPoint.x - HALFCIRCLEPICSIZE), (currentPoint.y - HALFCIRCLEPICSIZE));
        }
        print("done adding points.");

        // negate the image
        heatMap = negateImage(heatMap);

        // remap black/white with color spectrum from white over red, orange,
        // yellow, green to blue
        remap(heatMap);

        // blend image over lvlMap at opacity 40%
        BufferedImage output = loadImage(lvlMap);
        addImage(output, heatMap, 0.4f);

        // save image
//        saveImage(heatMap, outputFile);
        saveImage(output, outputFile);
        print("done creating heatmap.");
    }

    /**
     * remaps black and white picture with colors.
     * It uses the colors from SPECTRUMPIC. The whiter a pixel is, the more it
     * will get a color from the bottom of it. Black will stay black.
     *
     * @param heatMapBW black and white heat map
     */
    private void remap(BufferedImage heatMapBW) {
        BufferedImage colorGradiant = loadImage(SPECTRUMPIC);
        int width = heatMapBW.getWidth();
        int height = heatMapBW.getHeight();
        int gradientHight = colorGradiant.getHeight() - 1;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                // get heatMapBW color values:
                int rGB = heatMapBW.getRGB(i, j);

                // calculate multiplier to be applied to height of gradiant.
                float multiplier = rGB & 0xff; // blue
                multiplier *= ((rGB >>> 8)) & 0xff; // green
                multiplier *= (rGB >>> 16) & 0xff; // red
                multiplier /= 16581375; // 255f * 255f * 255f

                // apply multiplier
                int y = (int) (multiplier * gradientHight);

                // remap values
                // calculate new value based on whitenes of heatMap
                // (the whiter, the more a color from the top of colorGradiant
                // will be chosen.
                int mapedRGB = colorGradiant.getRGB(0, y);
                // set new value
                heatMapBW.setRGB(i, j, mapedRGB);
            }
        }
    }

    /**
     * returns a negated version of this image.
     *
     * @param img buffer to negate
     * @return negated buffer
     */
    private BufferedImage negateImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int rGB = img.getRGB(x, y);

                //Swaps values
                //i.e. 255, 255, 255 (white)
                //becomes 0, 0, 0 (black)
                int r = Math.abs(((rGB >>> 16) & 0xff) - 255); // red inverted
                int g = Math.abs(((rGB >>> 8) & 0xff) - 255); // green inverted
                int b = Math.abs((rGB & 0xff) - 255); // blue inverted
                
                // transform back to pixel value and set it
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }

    /**
     * changes all pixel in the buffer to the provided color.
     *
     * @param buff buffer
     * @param color color
     */
    private void paintInColor(BufferedImage buff, Color color) {
        Graphics2D g2 = buff.createGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, buff.getWidth(), buff.getHeight());
        g2.dispose();
    }

    /**
     * changes the opacity of the image.
     *
     * @param buff1 buffer to change opacity
     * @param opaque new opacity
     */
    private void makeTransparent(BufferedImage buff1, float opaque) {
        Graphics2D g2d = buff1.createGraphics();
        g2d.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC, opaque));
        g2d.drawImage(buff1, 0, 0, null);
        g2d.dispose();
    }

    /**
     * prints the contents of buff2 on buff1 with the given opaque value
     * starting at position 0, 0.
     *
     * @param buff1 buffer
     * @param buff2 buffer to add to buff1
     * @param opaque opacity
     */
    private void addImage( BufferedImage buff1, BufferedImage buff2, float opaque) {
        addImage(buff1, buff2, opaque, 0, 0);
    }

    private void addHeatSpot(BufferedImage buff1, float opaque, float multiplier, int x, int y) {
    	Short color = (short) (255 * opaque);
    	Color centerColour = new Color(color);
    	Color edgeColour = new Color( 0 );
    	float radius = 32;
    	Point2D center = new Point2D.Float(radius, radius);
        Color c1 = new Color(0,80,180,255);
        Color c2 = new Color(0,0,0,100);
        RadialGradientPaint paint = new RadialGradientPaint(radius/2,radius/2,radius/2,new float[]{0f,0.3f,1f},new Color[]{c1,c1,c2});
        Ellipse2D.Double ellips = new Ellipse2D.Double(x, y, radius, radius);
        
        
    	Graphics2D g2d = (Graphics2D)buff1.getGraphics();// createGraphics();
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
//        g2d.drawImage(buff2, x, y, null);
//        g2d.drawOval(x, y, (int)radius, (int)radius);
        g2d.setPaint(paint);
//        g2d.draw(ellips);
        g2d.fill( ellips );
        g2d.dispose();
    }
    
    /**
     * prints the contents of buff2 on buff1 with the given opaque value.
     *
     * @param buff1 buffer
     * @param buff2 buffer
     * @param opaque how opaque the second buffer should be drawn
     * @param x x position where the second buffer should be drawn
     * @param y y position where the second buffer should be drawn
     */
    private void addImage(BufferedImage buff1, BufferedImage buff2, float opaque, int x, int y) {
        Graphics2D g2d = buff1.createGraphics();
        g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
        g2d.drawImage(buff2, x, y, null);
        g2d.dispose();
    }

    /**
     * saves the image in the provided buffer to the destination.
     *
     * @param buff buffer to be saved
     * @param dest destination to save at
     */
    private void saveImage(BufferedImage buff, String dest) {
        try {
            File outputfile = new File(dest);
            ImageIO.write(buff, "png", outputfile);
        } catch (IOException e) {
            print("error saving the image: " + dest + ": " + e);
        }
    }

    /**
     * returns a BufferedImage from the Image provided.
     *
     * @param ref path to image
     * @return loaded image
     */
    private BufferedImage loadImage(String ref) {
        BufferedImage b1 = null;
        try {
            b1 = ImageIO.read(new File(ref));
        } catch (IOException e) {
            System.out.println("error loading the image: " + ref + " : " + e);
        }
        return b1;
    }

    /**
     * returns a hash calculated by the given point.
     *
     * @param p a point
     * @return hash value
     */
    private int getkey(Point p) {
        return ((p.x << 19) | (p.y << 7));
    }

    /**
     * prints string to sto.
     *
     * @param s string to print
     */
    private void print(String s) {
        System.out.println(s);
    }
}
