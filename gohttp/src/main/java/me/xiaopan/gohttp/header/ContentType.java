/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.gohttp.header;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.message.BasicHeader;

import java.util.HashMap;
import java.util.Map;

public class ContentType extends BasicHeader{
	public static final String NAME = "Content-Type";
	private String mimeType;
	private String charset;

	public ContentType(String value) {
        super(NAME, value);

        String[] element = value.split(";");
        if(element.length > 0){
            mimeType = element[0];
        }else{
            new IllegalArgumentException("value is not valid ("+value+")").printStackTrace();
            return;
        }
        if(element.length > 1){
            element = element[1].split("=");
            if(element.length > 1){
                charset = element[1];
            }
        }else{
            new IllegalArgumentException("No second elements ("+value+")").printStackTrace();
        }
    }

    public ContentType(String mimeType, String charset){
        super(NAME, mimeType+"; charset="+charset);
        this.mimeType = mimeType;
        this.charset = charset;
    }

	/**
	 * 获取类型
	 * @return 类型
	 */
	public String getMimeType() {
        return mimeType;
	}
	
	/**
	 * 获取字符集
	 * @return 字符集
	 */
	public String getCharset() {
		return charset;
	}

    /**
     * 获取字符集
     * @param defaultCharsetName 默认值
     * @return 字符集
     */
    public String getCharset(String defaultCharsetName) {
        return charset != null ? charset : defaultCharsetName;
    }

	/**
	 * 根据文件类型获取Content-Type
	 * @param fileType 文件类型，例如：.xml
	 * @return 例如：text/xml
	 */
	public static String getContentTypeByFileType(String fileType){
		Map<String, String> type = new HashMap<String, String>();
		type.put(".*", "application/octet-stream");
		type.put(".001", "application/x-001");
		type.put(".301", "application/x-301");
		type.put(".323", "text/h323");
		type.put(".906", "application/x-906");
		type.put(".907", "drawing/907");
		type.put(".a11", "application/x-a11");
		type.put(".acp", "audio/x-mei-aac");
		type.put(".ai", "application/postscript");
		type.put(".aif", "audio/aiff");
		type.put(".aifc", "audio/aiff");
		type.put(".aiff", "audio/aiff");
		type.put(".anv", "application/x-anv");
		type.put(".asa", "text/asa");
		type.put(".asf", "video/x-ms-asf");
		type.put(".asp", "text/asp");
		type.put(".asx", "video/x-ms-asf");
		type.put(".au", "audio/basic");
		type.put(".avi", "video/avi");
		type.put(".awf", "application/vnd.adobe.workflow");
		type.put(".biz", "text/xml");
		type.put(".bmp", "application/x-bmp");
		type.put(".bot", "application/x-bot");
		type.put(".c4t", "application/x-c4t");
		type.put(".c90", "application/x-c90");
		type.put(".cal", "application/x-cals");
		type.put(".cat", "application/vnd.ms-pki.seccat");
		type.put(".cdf", "application/x-netcdf");
		type.put(".cdr", "application/x-cdr");
		type.put(".cel", "application/x-cel");
		type.put(".cer", "application/x-x509-ca-cert");
		type.put(".cg4", "application/x-g4");
		type.put(".cgm", "application/x-cgm");
		type.put(".cit", "application/x-cit");
		type.put(".class", "java/*");
		type.put(".cml", "text/xml");
		type.put(".cmp", "application/x-cmp");
		type.put(".cmx", "application/x-cmx");
		type.put(".cot", "application/x-cot");
		type.put(".crl", "application/pkix-crl");
		type.put(".crt", "application/x-x509-ca-cert");
		type.put(".csi", "application/x-csi");
		type.put(".css", "text/css");
		type.put(".cut", "application/x-cut");
		type.put(".dbf", "application/x-dbf");
		type.put(".dbm", "application/x-dbm");
		type.put(".dbx", "application/x-dbx");
		type.put(".dcd", "text/xml");
		type.put(".dcx", "application/x-dcx");
		type.put(".der", "application/x-x509-ca-cert");
		type.put(".dgn", "application/x-dgn");
		type.put(".dib", "application/x-dib");
		type.put(".dll", "application/x-msdownload");
		type.put(".doc", "application/msword");
		type.put(".dot", "application/msword");
		type.put(".drw", "application/x-drw");
		type.put(".dtd", "text/xml");
		type.put(".dwf", "Model/vnd.dwf");
		type.put(".dwf", "application/x-dwf");
		type.put(".dwg", "application/x-dwg");
		type.put(".dxb", "application/x-dxb");
		type.put(".dxf", "application/x-dxf");
		type.put(".edn", "application/vnd.adobe.edn");
		type.put(".emf", "application/x-emf");
		type.put(".eml", "message/rfc822");
		type.put(".ent", "text/xml");
		type.put(".epi", "application/x-epi");
		type.put(".eps", "application/x-ps");
		type.put(".eps", "application/postscript");
		type.put(".etd", "application/x-ebx");
		type.put(".exe", "application/x-msdownload");
		type.put(".fax", "image/fax");
		type.put(".fdf", "application/vnd.fdf");
		type.put(".fif", "application/fractals");
		type.put(".fo", "text/xml");
		type.put(".frm", "application/x-frm");
		type.put(".g4", "application/x-g4");
		type.put(".gbr", "application/x-gbr");
		type.put(".", "application/x-");
		type.put(".gif", "image/gif");
		type.put(".gl2", "application/x-gl2");
		type.put(".gp4", "application/x-gp4");
		type.put(".hgl", "application/x-hgl");
		type.put(".hmr", "application/x-hmr");
		type.put(".hpg", "application/x-hpgl");
		type.put(".hpl", "application/x-hpl");
		type.put(".hqx", "application/mac-binhex40");
		type.put(".hrf", "application/x-hrf");
		type.put(".hta", "application/hta");
		type.put(".htc", "text/x-component");
		type.put(".htm", "text/html");
		type.put(".html", "text/html");
		type.put(".htt", "text/webviewhtml");
		type.put(".htx", "text/html");
		type.put(".icb", "application/x-icb");
		type.put(".ico", "image/x-icon");
		type.put(".ico", "application/x-ico");
		type.put(".iff", "application/x-iff");
		type.put(".ig4", "application/x-g4");
		type.put(".igs", "application/x-igs");
		type.put(".iii", "application/x-iphone");
		type.put(".img", "application/x-img");
		type.put(".ins", "application/x-internet-signup");
		type.put(".isp", "application/x-internet-signup");
		type.put(".IVF", "video/x-ivf");
		type.put(".java", "java/*");
		type.put(".jfif", "image/jpeg");
		type.put(".jpe", "image/jpeg");
		type.put(".jpe", "application/x-jpe");
		type.put(".jpeg", "image/jpeg");
		type.put(".jpg", "image/jpeg");
		type.put(".jpg", "application/x-jpg");
		type.put(".js", "application/x-javascript");
		type.put(".jsp", "text/html");
		type.put(".la1", "audio/x-liquid-file");
		type.put(".lar", "application/x-laplayer-reg");
		type.put(".latex", "application/x-latex");
		type.put(".lavs", "audio/x-liquid-secure");
		type.put(".lbm", "application/x-lbm");
		type.put(".lmsff", "audio/x-la-lms");
		type.put(".ls", "application/x-javascript");
		type.put(".ltr", "application/x-ltr");
		type.put(".m1v", "video/x-mpeg");
		type.put(".m2v", "video/x-mpeg");
		type.put(".m3u", "audio/mpegurl");
		type.put(".m4e", "video/mpeg4");
		type.put(".mac", "application/x-mac");
		type.put(".man", "application/x-troff-man");
		type.put(".math", "text/xml");
		type.put(".mdb", "application/msaccess");
		type.put(".mdb", "application/x-mdb");
		type.put(".mfp", "application/x-shockwave-flash");
		type.put(".mht", "message/rfc822");
		type.put(".mhtml", "message/rfc822");
		type.put(".mi", "application/x-mi");
		type.put(".mid", "audio/mid");
		type.put(".midi", "audio/mid");
		type.put(".mil", "application/x-mil");
		type.put(".mml", "text/xml");
		type.put(".mnd", "audio/x-musicnet-download");
		type.put(".mns", "audio/x-musicnet-stream");
		type.put(".mocha", "application/x-javascript");
		type.put(".movie", "video/x-sgi-movie");
		type.put(".mp1", "audio/mp1");
		type.put(".mp2", "audio/mp2");
		type.put(".mp2v", "video/mpeg");
		type.put(".mp3", "audio/mp3");
		type.put(".mp4", "video/mpeg4");
		type.put(".mpa", "video/x-mpg");
		type.put(".mpd", "application/vnd.ms-project");
		type.put(".mpe", "video/x-mpeg");
		type.put(".mpeg", "video/mpg");
		type.put(".mpg", "video/mpg");
		type.put(".mpga", "audio/rn-mpeg");
		type.put(".mpp", "application/vnd.ms-project");
		type.put(".mps", "video/x-mpeg");
		type.put(".mpt", "application/vnd.ms-project");
		type.put(".mpv", "video/mpg");
		type.put(".mpv2", "video/mpeg");
		type.put(".mpw", "application/vnd.ms-project");
		type.put(".mpx", "application/vnd.ms-project");
		type.put(".mtx", "text/xml");
		type.put(".mxp", "application/x-mmxp");
		type.put(".net", "image/pnetvue");
		type.put(".nrf", "application/x-nrf");
		type.put(".nws", "message/rfc822");
		type.put(".odc", "text/x-ms-odc");
		type.put(".out", "application/x-out");
		type.put(".p10", "application/pkcs10");
		type.put(".p12", "application/x-pkcs12");
		type.put(".p7b", "application/x-pkcs7-certificates");
		type.put(".p7c", "application/pkcs7-mime");
		type.put(".p7m", "application/pkcs7-mime");
		type.put(".p7r", "application/x-pkcs7-certreqresp");
		type.put(".p7s", "application/pkcs7-signature");
		type.put(".pc5", "application/x-pc5");
		type.put(".pci", "application/x-pci");
		type.put(".pcl", "application/x-pcl");
		type.put(".pcx", "application/x-pcx");
		type.put(".pdf", "application/pdf");
		type.put(".pdf", "application/pdf");
		type.put(".pdx", "application/vnd.adobe.pdx");
		type.put(".pfx", "application/x-pkcs12");
		type.put(".pgl", "application/x-pgl");
		type.put(".pic", "application/x-pic");
		type.put(".pko", "application/vnd.ms-pki.pko");
		type.put(".pl", "application/x-perl");
		type.put(".plg", "text/html");
		type.put(".pls", "audio/scpls");
		type.put(".plt", "application/x-plt");
		type.put(".png", "image/png");
		type.put(".png", "application/x-png");
		type.put(".pot", "application/vnd.ms-powerpoint");
		type.put(".ppa", "application/vnd.ms-powerpoint");
		type.put(".ppm", "application/x-ppm");
		type.put(".pps", "application/vnd.ms-powerpoint");
		type.put(".ppt", "application/vnd.ms-powerpoint");
		type.put(".ppt", "application/x-ppt");
		type.put(".pr", "application/x-pr");
		type.put(".prf", "application/pics-rules");
		type.put(".prn", "application/x-prn");
		type.put(".prt", "application/x-prt");
		type.put(".ps", "application/x-ps");
		type.put(".ps", "application/postscript");
		type.put(".ptn", "application/x-ptn");
		type.put(".pwz", "application/vnd.ms-powerpoint");
		type.put(".r3t", "text/vnd.rn-realtext3d");
		type.put(".ra", "audio/vnd.rn-realaudio");
		type.put(".ram", "audio/x-pn-realaudio");
		type.put(".ras", "application/x-ras");
		type.put(".rat", "application/rat-file");
		type.put(".rdf", "text/xml");
		type.put(".rec", "application/vnd.rn-recording");
		type.put(".red", "application/x-red");
		type.put(".rgb", "application/x-rgb");
		type.put(".rjs", "application/vnd.rn-realsystem-rjs");
		type.put(".rjt", "application/vnd.rn-realsystem-rjt");
		type.put(".rlc", "application/x-rlc");
		type.put(".rle", "application/x-rle");
		type.put(".rm", "application/vnd.rn-realmedia");
		type.put(".rmf", "application/vnd.adobe.rmf");
		type.put(".rmi", "audio/mid");
		type.put(".rmj", "application/vnd.rn-realsystem-rmj");
		type.put(".rmm", "audio/x-pn-realaudio");
		type.put(".rmp", "application/vnd.rn-rn_music_package");
		type.put(".rms", "application/vnd.rn-realmedia-secure");
		type.put(".rmvb", "application/vnd.rn-realmedia-vbr");
		type.put(".rmx", "application/vnd.rn-realsystem-rmx");
		type.put(".rnx", "application/vnd.rn-realplayer");
		type.put(".rp", "image/vnd.rn-realpix");
		type.put(".rpm", "audio/x-pn-realaudio-plugin");
		type.put(".rsml", "application/vnd.rn-rsml");
		type.put(".rt", "text/vnd.rn-realtext");
		type.put(".rtf", "application/msword");
		type.put(".rtf", "application/x-rtf");
		type.put(".rv", "video/vnd.rn-realvideo");
		type.put(".sam", "application/x-sam");
		type.put(".sat", "application/x-sat");
		type.put(".sdp", "application/sdp");
		type.put(".sdw", "application/x-sdw");
		type.put(".sit", "application/x-stuffit");
		type.put(".slb", "application/x-slb");
		type.put(".sld", "application/x-sld");
		type.put(".slk", "drawing/x-slk");
		type.put(".smi", "application/smil");
		type.put(".smil", "application/smil");
		type.put(".smk", "application/x-smk");
		type.put(".snd", "audio/basic");
		type.put(".sol", "text/plain");
		type.put(".sor", "text/plain");
		type.put(".spc", "application/x-pkcs7-certificates");
		type.put(".spl", "application/futuresplash");
		type.put(".spp", "text/xml");
		type.put(".ssm", "application/streamingmedia");
		type.put(".sst", "application/vnd.ms-pki.certstore");
		type.put(".stl", "application/vnd.ms-pki.stl");
		type.put(".stm", "text/html");
		type.put(".sty", "application/x-sty");
		type.put(".svg", "text/xml");
		type.put(".swf", "application/x-shockwave-flash");
		type.put(".tdf", "application/x-tdf");
		type.put(".tg4", "application/x-tg4");
		type.put(".tga", "application/x-tga");
		type.put(".tif", "image/tiff");
		type.put(".tif", "application/x-tif");
		type.put(".tiff", "image/tiff");
		type.put(".tld", "text/xml");
		type.put(".top", "drawing/x-top");
		type.put(".torrent", "application/x-bittorrent");
		type.put(".tsd", "text/xml");
		type.put(".txt", "text/plain");
		type.put(".uin", "application/x-icq");
		type.put(".uls", "text/iuls");
		type.put(".vcf", "text/x-vcard");
		type.put(".vda", "application/x-vda");
		type.put(".vdx", "application/vnd.visio");
		type.put(".vml", "text/xml");
		type.put(".vpg", "application/x-vpeg005");
		type.put(".vsd", "application/vnd.visio");
		type.put(".vsd", "application/x-vsd");
		type.put(".vss", "application/vnd.visio");
		type.put(".vst", "application/vnd.visio");
		type.put(".vst", "application/x-vst");
		type.put(".vsw", "application/vnd.visio");
		type.put(".vsx", "application/vnd.visio");
		type.put(".vtx", "application/vnd.visio");
		type.put(".vxml", "text/xml");
		type.put(".wav", "audio/wav");
		type.put(".wax", "audio/x-ms-wax");
		type.put(".wb1", "application/x-wb1");
		type.put(".wb2", "application/x-wb2");
		type.put(".wb3", "application/x-wb3");
		type.put(".wbmp", "image/vnd.wap.wbmp");
		type.put(".wiz", "application/msword");
		type.put(".wk3", "application/x-wk3");
		type.put(".wk4", "application/x-wk4");
		type.put(".wkq", "application/x-wkq");
		type.put(".wks", "application/x-wks");
		type.put(".wm", "video/x-ms-wm");
		type.put(".wma", "audio/x-ms-wma");
		type.put(".wmd", "application/x-ms-wmd");
		type.put(".wmf", "application/x-wmf");
		type.put(".wml", "text/vnd.wap.wml");
		type.put(".wmv", "video/x-ms-wmv");
		type.put(".wmx", "video/x-ms-wmx");
		type.put(".wmz", "application/x-ms-wmz");
		type.put(".wp6", "application/x-wp6");
		type.put(".wpd", "application/x-wpd");
		type.put(".wpg", "application/x-wpg");
		type.put(".wpl", "application/vnd.ms-wpl");
		type.put(".wq1", "application/x-wq1");
		type.put(".wr1", "application/x-wr1");
		type.put(".wri", "application/x-wri");
		type.put(".wrk", "application/x-wrk");
		type.put(".ws", "application/x-ws");
		type.put(".ws2", "application/x-ws");
		type.put(".wsc", "text/scriptlet");
		type.put(".wsdl", "text/xml");
		type.put(".wvx", "video/x-ms-wvx");
		type.put(".xdp", "application/vnd.adobe.xdp");
		type.put(".xdr", "text/xml");
		type.put(".xfd", "application/vnd.adobe.xfd");
		type.put(".xfdf", "application/vnd.adobe.xfdf");
		type.put(".xhtml", "text/html");
		type.put(".xls", "application/vnd.ms-excel");
		type.put(".xls", "application/x-xls");
		type.put(".xlw", "application/x-xlw");
		type.put(".xml", "text/xml");
		type.put(".xpl", "audio/scpls");
		type.put(".xq", "text/xml");
		type.put(".xql", "text/xml");
		type.put(".xquery", "text/xml");
		type.put(".xsd", "text/xml");
		type.put(".xsl", "text/xml");
		type.put(".xslt", "text/xml");
		type.put(".xwd", "application/x-xwd");
		type.put(".x_b", "application/x-x_b");
		type.put(".x_t", "application/x-x_t");
		type.put(".xyz", "chemical/x-pdb");
		type.put(".zip", "application/zip");
		return type.get(fileType.toLowerCase());
	}

    public static ContentType fromHttpMessage(HttpMessage httpMessage){
        Header firstHeader = httpMessage.getFirstHeader(NAME);
        if(firstHeader == null){
            return null;
        }
        return new ContentType(firstHeader.getValue());
    }
}
