package com.mr.rpa.assistant.util;

import javax.swing.*;
import javax.xml.ws.WebEndpoint;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Welcome extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Welcome(){
        JFrame welcome = new JFrame("系统");
        welcome.setResizable(false);
        Container container = welcome.getContentPane();
        container.setLayout(null);

        /*
         * 管理员用户登录
         */
        JMenuBar mbadmin = new JMenuBar();
        mbadmin.setLayout(new FlowLayout(FlowLayout.LEFT));
        mbadmin.setBounds(0, 0, 700, 30);

        JMenu mnadmin = new JMenu("管理员入口");

        JMenuItem mis = new JMenuItem("甲");
        JMenuItem mih = new JMenuItem("乙");
        JMenuItem miq = new JMenuItem("丙");
        JMenuItem mix = new JMenuItem("丁");

        mnadmin.add(mis);
        mnadmin.add(mih);
        mnadmin.add(miq);
        mnadmin.add(mix);

        mbadmin.add(mnadmin);
        mbadmin.setOpaque(true);
        /*
         * 欢迎界面
         */
        Icon cnimage = new ImageIcon("D:\\project\\workspace_taylor\\rpa-client\\src\\main\\resources\\icon.png");
        JLabel lbwelcome = new JLabel(cnimage,JLabel.CENTER);
        JPanel pnwelcome = new JPanel();
        pnwelcome.add(lbwelcome);
        pnwelcome.setBounds(0, 100, 700, 300);

        JButton btgoon = new JButton("继续");
        JPanel pngoon = new JPanel();
        pngoon.setLayout(new FlowLayout());
        pngoon.add(btgoon);
        pngoon.setBounds(0, 400, 700, 50);

        container.add(mbadmin);
        add(pnwelcome);
        container.add(pngoon);

        welcome.setBounds(250, 100, 700, 500);
        welcome.show();
        welcome.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
    }
    public static void main(String[] s){
        new Welcome();
    }
}
