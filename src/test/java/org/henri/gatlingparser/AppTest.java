package org.henri.gatlingparser;

import org.junit.Test;

public class AppTest 
{
    private static final String FIRST = "D:\\Test\\Dropbox\\F2012-188 MAIF Audit d'un applicatif frontal web\\Documents de travail\\Tests de charge\\gatling\\results\\run20120619183317_slapp197_50u_100r_4l\\simulation.log";
    private static final String FINAL = "D:\\Test\\Dropbox\\F2012-188 MAIF Audit d'un applicatif frontal web\\Documents de travail\\Tests de charge\\gatling\\results\\run20120725142501_slapp256a_50u_100r_5d_final\\simulation.log";
    private static final String U25 = "D:\\Test\\Dropbox\\F2012-188 MAIF Audit d'un applicatif frontal web\\Documents de travail\\Tests de charge\\gatling\\results\\run20120725153139_slapp256a_25u_3r_5d_final\\simulation.log";

//    @Test
    public void testFIRST() throws Exception
    {
        App.main(FIRST);
    }
    

//    @Test
    public void testFINAL() throws Exception
    {
        App.main(FINAL);
    }
    
    @Test
    public void test25() throws Exception
    {
        App.main(U25);
    }
    
}
