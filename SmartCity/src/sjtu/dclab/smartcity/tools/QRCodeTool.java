package sjtu.dclab.smartcity.tools;

import java.util.Hashtable;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
/**
 * Created by zwsr/Zhang Wei on 2015/8/4.
 */
public class QRCodeTool {
    private static int QR_WIDTH = 200, QR_HEIGHT = 200;
    public QRCodeTool(){
    }

    //ͨ�������ַ���src���ɶ�Ӧ�Ķ�ά��
    //���ص�ͼƬ����ΪBitMap,Ĭ�ϴ�С����������,Ĭ��Ϊ200x200
    public static Bitmap createQRBitmap(String src)
    {
        Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
        try
        {
            //���ж�src�Ϸ���
            if (src == null || src.equals(src) || src.length() < 1)
            {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

            BitMatrix bitMatrix = new QRCodeWriter().encode(src, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];

            for (int y = 0; y < QR_HEIGHT; y++)
            {
                for (int x = 0; x < QR_WIDTH; x++)
                {
                    if (bitMatrix.get(x, y))
                    {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    }
                    else
                    {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            //���ɶ�ά��ͼƬ�ĸ�ʽ��ʹ��ARGB_8888

            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);


        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }
        return bitmap;

    }
}
