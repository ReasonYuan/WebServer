package wx.mongodb;

/**
 * Hello world!
 *
 */
public class App 
{
	//服务器ip
//	public static final String serverIp = "114.55.36.11";
	
	//本地ip
	public static final String serverIp = "localhost";
	
    public static void main( String[] args )
    {
    	
        UIUtils.createDbInsertUI();
        DBUtils.getCollection("test","words_1");
    }
}
