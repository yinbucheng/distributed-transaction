package cn.mst.server.base;

/**
 * Mst服务端属性清理器
 * @ClassName MstServerAttributeClean
 * @Author buchengyin
 * @Date 2018/12/19 19:31
 **/
public class MstServerAttributeClean {
    private static final int size =240;
    private static String[] tokens = new String[size];
    private static volatile int prev = size -1;
    private static volatile int cur = 0;

    public static void addToken(String token){
        token[prev]=token;
    }

    public static void work(){
        for(int i=0;i<Integer.MAX_VALUE;i++){
            String token = tokens[cur];
            if(token!=null){
                MstServerAttributeHolder.removeChannelHandlerContext(token);
                MstServerAttributeHolder.isRollBack(token);
                tokens[cur]=null;
            }
            prev =cur;
            cur=cur==size-1?0:cur+1;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
