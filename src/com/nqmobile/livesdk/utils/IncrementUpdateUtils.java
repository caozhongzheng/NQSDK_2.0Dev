package com.nqmobile.livesdk.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

/** 
 * Android Apk压缩解压缩 
 * 用于增量升级对apk文件的处理，需要先解压本地老版本，剔除渠道号文件(如果有)，再压缩得到无渠道号的旧版本。用此旧版本和增量包合并生成新的apk包进行安装
 */ 
public class IncrementUpdateUtils {   
    // 增量升级的工作目录
    //public static String INCREASE_UPDATE_PATH = "/IncrementUpdate/";
    public static String INCREASE_UPDATE_PATH = "/" + Environment.DIRECTORY_DOWNLOADS + "/";
    
	public static String CHANNEL_FILENAME = "res/raw/channel.dat";//需要剔除的渠道号文件名
	public static String SIGNITURE_RSA_FILE = "META-INF/CERT.RSA";//测试剔除的签名文件
	public static String SIGNITURE_DSA_FILE = "META-INF/CERT.DSA";//测试剔除的签名文件    
	public static String SIGNITURE_FILE = "META-INF";//测试剔除的签名文件 
	public static String PATCH_SUFFIX = ".patch"; //path包文件名
	public static String FULLPACKAGE_SUFFIX = ".apk"; //新的全量包或者生成的新全量包文件名
    /**
     * 循环遍历apk文件查找是否包含渠道号文件
     * @throws Exception
     */
    public static boolean hasChannelFileInApk(File apkFile) throws ZipException, IOException {
        ZipFile zfile = new ZipFile(apkFile);
        ZipEntry ze = zfile.getEntry(CHANNEL_FILENAME);
        boolean ret;
        if (ze == null) {
        	ret = false;
        } else {
        	ret = true;
        }

        zfile.close();
        return ret;
    }
    
    /**
     * 解压一个压缩文档 到指定位置 ，并且剔除渠道号文件
     * @param zipFile 要压缩的zip/apk文件
     * @param zipFileString 指定解压的目录
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
            
            //过滤掉渠道号文件和签名文件
            String filename = ze.getName();
            //if (filename.contains(CHANNEL_FILENAME) || filename.contains(SIGNITURE_RSA_FILE)) {
            if (filename.contains(CHANNEL_FILENAME)) {
            	Log.v("ljc","find files need to removed");
            	continue;
            }
            
            String file = new StringBuilder(folderPath).append(File.separator).append(filename).toString();
            //如果父目录不存在，创建
            String dir = file.substring(0,file.lastIndexOf("/"));
            File fileDir = new File(dir);
            if(!fileDir.exists()){
            	fileDir.mkdirs();
            }
            OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(file)));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }

        zfile.close();
        return 0;
    }
        
    public static int ConvertFile(File zipFile, File destApk) throws ZipException, IOException {
        ZipFile zfile = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> zList = zfile.entries();
        ZipEntry ze = null;
        
        FileOutputStream os = new FileOutputStream(destApk);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
        
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                continue;
            }
            
            //过滤掉渠道号文件和签名文件
            String filename = ze.getName();
            if (filename.contains(CHANNEL_FILENAME) || filename.contains(SIGNITURE_FILE)) {
            //if (filename.contains(CHANNEL_FILENAME)) {
            	Log.v("ljc","find files need to removed");
            	continue;
            }
            
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
			zos.putNextEntry(ze);
            int len;  
            byte[] buffer = new byte[4096];  
              
            while((len=is.read(buffer)) != -1)  
            {  
            	zos.write(buffer, 0, len);  
            }  
              
            is.close();
			zos.closeEntry();
        }
		zos.close();
        zfile.close();
        return 0;
    }

    /** 
     * 把剔除掉渠道号的解压缩目录压缩成 zip/apk文件 
     * @param srcFileString 要压缩的文件夹名字 
     * @param zipFileString 指定压缩的目的文件名
     * @throws Exception 
     */  
    public static void zipFolder(String srcFileString, String zipFileString)throws Exception {           
    	ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));         
        File file = new File(srcFileString);  
  
        //压缩  
        //ZipFiles(file.getParent()+java.io.File.separator, file.getName(), outZip);
        //ZipFiles(file.getAbsolutePath(), "", outZip);
        String fileList[] = file.list(); 
        //如果有子文件, 遍历子文件  
        for (int i = 0; i < fileList.length; i++) {
            ZipFiles(file.getAbsolutePath()+ File.separator, fileList[i], outZip);  
        }
        
        outZip.finish();  
        outZip.close();  
      
    }//end of func
    
    /** 
     * 压缩文件 , 如有子目录，递归压缩
     * @param folderString 
     * @param fileString 
     * @param zipOutputSteam 
     * @throws Exception 
     */  
    private static void ZipFiles(String folderString, String fileString, java.util.zip.ZipOutputStream zipOutputSteam)throws Exception{     	
        if(zipOutputSteam == null)  
            return;  
          
        File file = new File(folderString+fileString);  
          
        //判断是不是文件  
        if (file.isFile()) {  
            ZipEntry zipEntry =  new ZipEntry(fileString);  
            FileInputStream inputStream = new FileInputStream(file);  
            zipOutputSteam.putNextEntry(zipEntry);  
              
            int len;  
            byte[] buffer = new byte[4096];  
              
            while((len=inputStream.read(buffer)) != -1)  
            {  
                zipOutputSteam.write(buffer, 0, len);  
            }  
              
            inputStream.close();
            zipOutputSteam.closeEntry();  
        } else {                
            //文件夹的方式,获取文件夹下的子文件  
            String fileList[] = file.list();  
              
            //如果没有子文件, 则添加进去即可  
            if (fileList.length <= 0) {  
                ZipEntry zipEntry =  new ZipEntry(fileString+java.io.File.separator); 
                zipOutputSteam.putNextEntry(zipEntry);  
                zipOutputSteam.closeEntry();                  
            }  
              
            //如果有子文件, 遍历子文件  
            for (int i = 0; i < fileList.length; i++) {  
                ZipFiles(folderString, fileString+java.io.File.separator+fileList[i], zipOutputSteam);  
            }//end of for  
      
        }//end of if  
          
    }//end of func
        
    public static void copyFile(String oldPath, String newPath) { 
    	InputStream inStream = null;
    	FileOutputStream fs = null;
        try {   
            int byteread = 0;   
            File oldfile = new File(oldPath);   
            if (oldfile.exists()) {
                inStream = new FileInputStream(oldPath); 
                fs = new FileOutputStream(newPath);   
                byte[] buffer = new byte[1444];   

                while ( (byteread = inStream.read(buffer)) != -1) {   
                    fs.write(buffer, 0, byteread);   
                }   
            }   
        }   
        catch (Exception e) {   
            System.out.println("复制单个文件操作出错");   
            e.printStackTrace();   
   
        } finally {
        	FileUtil.closeStream(inStream);
        	FileUtil.closeStream(fs);
        }
   
    } 
  
    
    /** 删除非空目录  dir
    */
    public static boolean deleteDir(File dir) {

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }    
       
    public static String getPackageSignature(Context context) {  
    	PackageManager pm = context.getPackageManager();
    	if (pm == null) {
    		return null;
    	}
    	
        try {       	
			PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            android.content.pm.Signature sign = packageInfo.signatures[0];
            byte[] signature = sign.toByteArray();
            
            CertificateFactory certFactory = CertificateFactory
                    .getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));

            String publickey = cert.getPublicKey().toString();
	        Log.v("ljc", "publickey before convert = " + publickey);
	        
	        if (publickey.contains("modulus=")) {
	        	publickey = publickey.substring(publickey.indexOf("modulus=") + 8, publickey.indexOf(","));
	        } else if (publickey.contains("modulus: ")){
	        	publickey.substring(publickey.indexOf("modulus: ") + 9, publickey.indexOf("\n", publickey.indexOf("modulus:")));
	        } else {
	        	
	        }
            Log.v("ljc", "return publickey = " + publickey);
            return publickey;

        } catch (CertificateException e) {
            e.printStackTrace();
        }catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return null;
    }

	@SuppressWarnings("finally")
	public static String getPackageMd5(Context context) {  
		String md5 = null;
		//File destfile = null;
        try{  
        	PackageManager pm = context.getPackageManager();
        	
            String src = pm.getApplicationInfo(context.getPackageName(),0).sourceDir;
            if (src == null) {
            	return null;
            }
            
//            String SDCardPath = CommonMethod.getSDcardPath(context);
//            src = new StringBuilder().append(SDCardPath).append(IncrementUpdateUtils.INCREASE_UPDATE_PATH).append("ZTE_Custom_Live_1.1_135-SubCoopID_1979_1.apk").toString();
            md5 = MD5.calculateMD5(new File(src));
/*
            //File file = new File(src);
            File updateDir = null;
            String SDCardPath = CommonMethod.getSDcardPath(context);
			String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			String destPath = new StringBuilder().append(SDCardPath).append(IncrementUpdateUtils.INCREASE_UPDATE_PATH).append(time).toString();
			updateDir = new File(destPath);
			if(!updateDir.exists()){
				updateDir.mkdirs();
			} 			
			//test
			src = new StringBuilder().append(SDCardPath).append(IncrementUpdateUtils.INCREASE_UPDATE_PATH).append("ZTE_Custom_Live_1.1_135-SubCoopID_1979_1.apk").toString();
			
			File file = new File(src);	
			
			if (hasChannelFileInApk(file)) {// 如果原安装包文件包含渠道号信息，需要重新生成old安装包
			   	//IncrementUpdateUtils.upZipFile(file, destPath);
			   	//String destApk = destPath.substring(0,destPath.lastIndexOf("/")) + IncrementUpdateUtils.GENERATED_OLDAPK_NAME;
				String destApk = destPath.substring(0,destPath.lastIndexOf("/") + 1) +  System.currentTimeMillis() + ".apk";
			   	File dstFile = new File(destApk);
			   	NqLog.v("ljctest","getPackageMd5:src =" + src);
			   	NqLog.v("ljctest","getPackageMd5:destApk =" + destApk);
			   	ConvertFile(file, dstFile);
			   	//IncrementUpdateUtils.zipFolder(destPath, destApk);
			   	
			   	//把渠道号写入xml
			   	PreferenceDataHelper helper = PreferenceDataHelper.getInstance(context);
			   	if (helper.getStringValue(PreferenceDataHelper.KEY_CHANEL_ID).isEmpty()) {
				   	String channelId = CommonMethod.getChannelId((ContextWrapper)context);
				   	helper.setStringValue(PreferenceDataHelper.KEY_CHANEL_ID, channelId);
			   	}
			   	
			   	//deleteDir(updateDir);
			   	destfile = new File(destApk);
			   	md5 = MD5.calculateMD5(destfile);
			} else {// 当前安装版本就不包含渠道号文件，不用重新生成旧包
				md5 = MD5.calculateMD5(new File(src));				                
			}
        } catch (NameNotFoundException e) {  
            e.printStackTrace();
        } catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//destfile.delete();
			return md5;
		}*/
        } catch (NameNotFoundException e) {  
            e.printStackTrace();
        } finally {
			return md5;
        }
    }
}
