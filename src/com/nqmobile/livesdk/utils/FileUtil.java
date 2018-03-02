package com.nqmobile.livesdk.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.nqmobile.livesdk.commons.log.NqLog;

/**
 * 文件工具类
 *
 * @author changxiaofei
 * @time 2013-12-7 下午5:30:03
 */
public class FileUtil {
    /**
     * 将Bitmap写入文件
     *
     * @param bmp
     * @param file
     * @return
     */
    public static boolean writeBmpToFile(Bitmap bmp, File file) {
    	if (Tools.stringEquals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
			if (!file.getParentFile().exists()) 
				file.getParentFile().mkdirs();
		}
        if (file.exists()) {
            file.delete();
        }
        String name = file.getName();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            if (null != bmp) {// 全存为jpg
            	String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
//				if (".JPEG".equalsIgnoreCase(geShi) || ".JPG".equalsIgnoreCase(geShi)) {
                bmp.compress(getFormat(ext), 100, bos);
                bos.flush();
                bos.close();
                /*				} else if (".PNG".equalsIgnoreCase(geShi)) {
									bmp.compress(CompressFormat.PNG, 100, bos);
									bos.flush();
									bos.close();
								}*/
                return true;
            } else
                bos.close();
        } catch (Exception e) {
            NqLog.v("FileUtil.writeToFile error" + e.toString());
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static CompressFormat getFormat(String ext) {
		// TODO Auto-generated method stub
    	ext = ext.toLowerCase();
    	if(ext.equals("jpg") || ext.equals("jpeg"))
    		return CompressFormat.JPEG;
    	else if(ext.equals("png"))
    		return CompressFormat.PNG;
		return CompressFormat.PNG;
	}

	/**
     * InputStream写入文件
     *
     * @param fileName
     * @param is
     * @return
     */
    public static boolean writeStreamToFile(String fileName, InputStream is) {
        if (TextUtils.isEmpty(fileName) || is == null){
        	return false;
        }
        
        boolean result = false;
    	OutputStream os = null;
        try {
            os = new FileOutputStream(fileName);
            byte buffer[] = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            result = true;
        } catch (Exception e) {
        	NqLog.e(e);
        } finally {
            closeStream(os);
        }
        
        return result;
    }

    /**
     * 删除文件。
     *
     * @param filePath 带文件名的绝对路径
     * @return boolean
     */
    public static boolean delFile(String absoluteFilePath) {
        if (TextUtils.isEmpty(absoluteFilePath))
            return false;
        boolean s = false;
        try {
            File updateFile = new File(absoluteFilePath);
            if (updateFile.exists() && updateFile.isFile()) {
                s = updateFile.delete();
            }
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static boolean delFile(List<String> absoluteFilePath) {
        if (absoluteFilePath != null && absoluteFilePath.size() > 0)
            for (int i = 0; i < absoluteFilePath.size(); i++)
                delFile(absoluteFilePath.get(i));
        return false;
    }

    /**
     * 删除目录下的所有文件。
     *
     * @param dir 目录
     * @return boolean
     */
    public static boolean delAllFilesInFolder(String dir) {
        boolean s = false;
        File delfolder = new File(dir);
        File oldFile[] = delfolder.listFiles();
        try {
            for (int i = 0; i < oldFile.length; i++) {
                if (oldFile[i].isDirectory()) {
                    delAllFilesInFolder(dir + File.separator + oldFile[i].getName() + "//"); // 递归清空子文件夹
                }
                oldFile[i].delete();
            }
            s = true;
        } catch (Exception e) {
            NqLog.e("FileUtil.delAllFilesInFolder" + e.toString());
        }
        return s;
    }

    /**
     * 解压缩功能. 将ZIP_FILENAME文件解压到ZIP_DIR目录下.
     *
     * @throws Exception
     */
    public static int upZipFile(File zipFile, String folderPath) throws ZipException, IOException {
        ZipFile zfile = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                String dirstr = folderPath + File.separator + ze.getName();
                dirstr = new String(dirstr.getBytes("8859_1"), "UTF-8");
                Log.d("upZipFile", "str = " + dirstr);
                File f = new File(dirstr);
                f.mkdir();
                continue;
            }
            String file = new StringBuilder(folderPath).append(File.separator).append(ze.getName()).toString();
            Log.d("upZipFile", "file = " + file);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(file)));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zipFile.delete();
        zfile.close();
        return 0;
    }

    /**
     * 把zip文件中文件名包含特定字符串的文件解压到指定目录
     * @param zipFile
     * @param destFilePath
     * @return
     * @throws ZipException
     * @throws IOException
     */
    public static int upZipSingleFile(File zipFile, String pattern, String destFilePath) throws ZipException, IOException {
        ZipFile zfile = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                continue;
            }
            
            String filename = ze.getName();
            if (filename.contains(pattern)) {
            	Log.i("ljc","find dest file =" + filename);
            	            
            	            
	            OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(destFilePath)));
	            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
	            int readLen = 0;
	            while ((readLen = is.read(buf, 0, 1024)) != -1) {
	                os.write(buf, 0, readLen);
	            }
	            is.close();
	            os.close();
	            
	            break;
            }
        }

        zfile.close();
        return 0;
    }
    
    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    public static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                try {
                    // substr.trim();
                    substr = new String(substr.getBytes("8859_1"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ret = new File(ret, substr);
            }
            Log.d("upZipFile", "1ret = " + ret);
            if (!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length - 1];
            try {
                // substr.trim();
                substr = new String(substr.getBytes("8859_1"), "UTF-8");
                Log.d("upZipFile", "substr = " + substr);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ret = new File(ret, substr);
            Log.d("upZipFile", "2ret = " + ret);
            return ret;
        }
        return ret;
    }

    /**
     * 文件复制
     *
     * @param sourceFile
     * @param targetFile
     * @return
     */
    public static boolean copyFile(String sourceFile, String targetFile) {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            File tempSourceFile = new File(sourceFile);
            File tempTargetFile = new File(targetFile);
            if (tempSourceFile.exists() && tempSourceFile.isFile() && tempTargetFile.getParentFile().exists() && tempTargetFile.getParentFile().isDirectory()) {
                if (tempTargetFile.exists()) {
                    tempTargetFile.delete();
                }
                // 新建文件输入流并对它进行缓冲
                inBuff = new BufferedInputStream(new FileInputStream(tempSourceFile));
                // 新建文件输出流并对它进行缓冲
                outBuff = new BufferedOutputStream(new FileOutputStream(tempTargetFile));
                // 缓冲数组
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = inBuff.read(b)) != -1) {
                    outBuff.write(b, 0, len);
                }
                // 刷新此缓冲的输出流
                outBuff.flush();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (inBuff != null)
                try {
                    inBuff.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            if (outBuff != null)
                try {
                    outBuff.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return false;
    }

    /**
     * 文件复制
     *
     * @param source input stream
     * @param target output stream
     * @return
     */
    public static void copyFile(FileInputStream input, FileOutputStream output) throws IOException {
    	BufferedInputStream bufferedinputstream = new BufferedInputStream(input);
        BufferedOutputStream bufferedoutputstream = null;
        try {
            bufferedoutputstream = new BufferedOutputStream(output);
            byte b[] = new byte[5120];
            do {
                int offset = bufferedinputstream.read(b);
                if (offset != -1) {
                    bufferedoutputstream.write(b, 0, offset);
                } else {
                    return;
                }
            } while (true);
        } finally {
            if(bufferedoutputstream != null) {
                bufferedoutputstream.flush();
                bufferedoutputstream.close();
            }

            if(bufferedinputstream != null)
                bufferedinputstream.close();

            if(output != null) {
                output.close();
            }

            if(input != null)
                input.close();
        }
    }
    /**
     * 创建文件夹
     *
     * @param filepath
     * @return
     */
    public static boolean createFolder(String path) {
        if (path == null || TextUtils.isEmpty(path))
            return false;
        File file = new File(path);
        File folder = new File(path.substring(0, path.lastIndexOf("/")));
        if (folder.mkdirs() || folder.isDirectory()) {
            if (!file.exists() && file.isDirectory()) {
                file.mkdir();
                return true;
            }
        }
        return false;

    }
    
    /**
     * 关闭流
     * @param closeable
     */
    public static void closeStream(Closeable closeable){
		if (closeable != null)
			try {
				closeable.close();
			} catch (IOException e) {
				NqLog.e(e);
			}
	}

    public static boolean isFileExists(String filePath){
        boolean result = false;

        if (!TextUtils.isEmpty(filePath)){
            File file = new File(filePath);
            if (file.exists()){
                result = true;
            }
        }

        return result;
    }
}
