package tetris;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.awt.Window.Type;

public class Tetris implements Runnable{

	private JFrame frmlow;
	//画布对象
	private MoveCanvas mCanvas;
	
	
	//作弊标识
	public int nMark=0;
	//线程
    Thread thread;
    //线程暂停标识
    private boolean suspend =false;
	//标签
    JLabel lblScore;
    private JButton btnStart;
	
	//设置键盘事件监听器
	class MyKeyListener extends KeyAdapter{
		 //重载键盘响应方法
		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			switch(e.getKeyCode()){
			case KeyEvent.VK_LEFT:
				mCanvas.left();
			    break;
			case KeyEvent.VK_RIGHT:
				mCanvas.right();
				break;
			case KeyEvent.VK_DOWN:
				mCanvas.down();
		        break;
			case KeyEvent.VK_UP:
				mCanvas.rotate();
				break;
			}
		}
	}
	//设置鼠标事件监听器
	class stopMouseListener extends MouseAdapter{
			//重载鼠标响应方法
			public void mouseClicked(MouseEvent e){
				//System.out.println("stop");
					suspend=false;
					thread.interrupt();
			}
		}
	class startMouseListener extends MouseAdapter{
		//重载鼠标响应方法
		public void mouseClicked(MouseEvent e){
			//System.out.println("stop");
				suspend=true;synchronized (thread) {
	                thread.notifyAll();
	            }
			frmlow.requestFocus();
		}
	}
	class restartMouseListener extends MouseAdapter{
		public void mouseClicked(MouseEvent e){
			if(mCanvas.bDebug == false)
			 {
				 mCanvas.blockType=(int)(Math.random()*100)%7;
			 }
			 else
			 {
				 mCanvas.blockType=4;
			 }
			mCanvas.rotateState=(int)(Math.random()*100)%4;
			mCanvas.newblock();
			mCanvas.newmap();
			mCanvas.setwall();
			mCanvas.repaint();
			
			frmlow.requestFocus();
			suspend=false;

			//分数清零
			mCanvas.score=0;
			lblScore.setText("分数为"+mCanvas.score);
			
		}
	}
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Tetris window = new Tetris();
					window.frmlow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Tetris() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmlow = new JFrame();
		frmlow.setResizable(false);
		frmlow.setType(Type.POPUP);
		frmlow.setTitle("\u53F2\u4E0A\u6700low\u4FC4\u7F57\u65AF\u65B9\u5757");
		frmlow.setBounds(100,100, 756, 727);
		frmlow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmlow.getContentPane().setLayout(null);
		//面板获取焦点
		frmlow.setFocusable(true);
		
		//Canvas
		mCanvas=new MoveCanvas();
		mCanvas.setBounds(10, 7, 513, 640);
		
		//面板
		JPanel panelTetris = new JPanel();
		panelTetris.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelTetris.setBounds(0,0,533,683);
		frmlow.getContentPane().add(panelTetris);
		panelTetris.setLayout(null);
		panelTetris.add(mCanvas);
		
		lblScore = new JLabel("\u5206\u6570\u4E3A0");
		lblScore.setHorizontalAlignment(SwingConstants.CENTER);
		lblScore.setBounds(581, 143, 118, 18);
		frmlow.getContentPane().add(lblScore);
		
		JButton btnRestart = new JButton("\u91CD\u65B0\u5F00\u59CB");
		btnRestart.setBounds(581, 333, 113, 69);
		frmlow.getContentPane().add(btnRestart);
		btnRestart.addMouseListener(new restartMouseListener());
		
		JButton btnStop = new JButton("\u6682\u505C");
		btnStop.setBounds(581, 533, 113, 69);
		frmlow.getContentPane().add(btnStop);
		
		JButton btnaStart = new JButton("\u5F00\u59CB");
		btnaStart.setBounds(581, 434, 113, 69);
		frmlow.getContentPane().add(btnaStart);
		
		JButton btnfivefiveopen = new JButton("55\u5F00");
		btnfivefiveopen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				nMark++;
				if(nMark%2==1)
				{
				mCanvas.bDebug =true;
				}
				else
					mCanvas.bDebug =false;
				frmlow.requestFocus();
			}
		});
		btnfivefiveopen.setBounds(581, 251, 113, 51);
		frmlow.getContentPane().add(btnfivefiveopen);
		
		frmlow.requestFocus();
		//开始
		thread=new Thread(this);
		thread.start();
		
		//注册监听器
		frmlow.addKeyListener(new MyKeyListener());
		btnStop.addMouseListener(new stopMouseListener());
		btnaStart.addMouseListener(new startMouseListener());
	}
	
	//重载run
	public void run(){
		while(true){
				if(!suspend){
					synchronized (thread) {
					try{
						thread.wait();
					}catch(InterruptedException e){}
					}
				}
				else{
					mCanvas.down();
					lblScore.setText("分数为"+mCanvas.score);
					//消行
					mCanvas.delline();
					if(mCanvas.gameOver())
						suspend=false;
					try{
						Thread.sleep(400);
					}catch(InterruptedException e){}
				}
		}
	}
}
//画布类
class MoveCanvas extends Canvas{
	//blockType代表方块的类型
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//分数
	public int  score=0;
	//rotateState代表方块的状态
	public int rotateState;
	//blockType为方块形状
	public int blockType;
	
	//下一个的状态
	private int rotateState1;
	private int blockType1;
	
	//是否作弊
	public boolean bDebug = false;
	
	//初始位置
	private int x;
	private int y;
	
	//游戏区域行数和列数
	public static final int row=21;
	public static final int column=12;
	
	//是否生成新方块的标记
	int isReborn;
	
	// 地图
	private int [][]map=new int[row][column];
	//开始的标志
	private int flagStart=0;
	
	//颜色
	Color c;
	Color c1;
	
    //方块数组
	private final int shapes[][][]=new int[][][]{
		//i
		{
			{ 0, 0, 0, 0,
			  1, 1, 1, 1, 
			  0, 0, 0, 0, 
			  0, 0, 0, 0 },
			{ 0, 1, 0, 0,
			  0, 1, 0, 0, 
			  0, 1, 0, 0, 
			  0, 1, 0, 0 },
			{ 0, 0, 0, 0, 
			  1, 1, 1, 1, 
			  0, 0, 0, 0, 
			  0, 0, 0, 0 },
			{ 0, 1, 0, 0,
			  0, 1, 0, 0, 
			  0, 1, 0, 0, 
			  0, 1, 0, 0 }
		},
		//s
		{
		    { 0, 1, 1, 0, 
		      1, 1, 0, 0, 
		      0, 0, 0, 0,
		      0, 0, 0, 0 },
			{ 1, 0, 0, 0, 
		      1, 1, 0, 0, 
		      0, 1, 0, 0, 
		      0, 0, 0, 0 },
			{ 0, 1, 1, 0,
		      1, 1, 0, 0, 
		      0, 0, 0, 0, 
		      0, 0, 0, 0 },
			{ 1, 0, 0, 0,
		      1, 1, 0, 0, 
		      0, 1, 0, 0, 
		      0, 0, 0, 0 }
			
		},
		//z
		{ 
			{ 1, 1, 0, 0, 
		      0, 1, 1, 0, 
		      0, 0, 0, 0,
		      0, 0, 0, 0 },
			{ 0, 1, 0, 0, 
		      1, 1, 0, 0, 
		      1, 0, 0, 0, 
		      0, 0, 0, 0 },
			{ 1, 1, 0, 0, 
		      0, 1, 1, 0, 
		      0, 0, 0, 0, 
		      0, 0, 0, 0 },
			{ 0, 1, 0, 0, 
		      1, 1, 0, 0, 
		      1, 0, 0, 0, 
		      0, 0, 0, 0 } 
		},
		//j
		{ 
			{ 0, 1, 0, 0, 
			  0, 1, 0, 0, 
			  1, 1, 0, 0, 
			  0, 0, 0, 0 },
			{ 1, 0, 0, 0, 
			  1, 1, 1, 0, 
			  0, 0, 0, 0, 
			  0, 0, 0, 0 },
			{ 1, 1, 0, 0, 
			  1, 0, 0, 0, 
			  1, 0, 0, 0, 
			  0, 0, 0, 0 },
			{ 1, 1, 1, 0,
			  0, 0, 1, 0,
			  0, 0, 0, 0,
			  0, 0, 0, 0 } 
		},
		//o
		{ 
			{ 1,1, 0, 0, 
		      1,1, 0, 0, 
		      0,0, 0, 0, 
		      0, 0, 0, 0 },
			{ 1,1, 0, 0, 
		      1,1, 0, 0, 
		      0,0, 0, 0, 
		      0, 0, 0, 0 },
			{ 1,1, 0, 0, 
		      1,1, 0, 0, 
		      0,0, 0, 0, 
		      0, 0, 0, 0 },
			{ 1,1, 0, 0, 
		      1,1, 0, 0, 
		      0,0, 0, 0, 
		      0, 0, 0, 0 },
		},
		//l
		{
			{ 1, 0, 0, 0, 
		      1, 0, 0, 0, 
		      1, 1, 0, 0, 
		      0, 0, 0, 0 },
			{ 1, 1, 1, 0,
		      1, 0, 0, 0,
		      0, 0, 0, 0, 
		      0, 0, 0, 0 },
			{ 1, 1, 0, 0, 
		      0, 1, 0, 0,
		      0, 1, 0, 0, 
		      0, 0, 0, 0 },
			{ 0, 0, 1, 0, 
		      1, 1, 1, 0, 
		      0, 0, 0, 0,
		      0, 0, 0, 0 }
		},
		//t
		{ 
			{ 0, 1, 0, 0, 
		      1, 1, 1, 0, 
		      0, 0, 0, 0, 
		      0, 0, 0, 0 },
			{ 0, 1, 0, 0,
		      1, 1, 0, 0, 
		      0, 1, 0, 0, 
		      0, 0, 0, 0 },
			{ 1, 1, 1, 0,
		      0, 1, 0, 0,
		      0, 0, 0, 0,
		      0, 0, 0, 0 },
			{ 0, 1, 0, 0, 
		      0, 1, 1, 0, 
		      0, 1, 0, 0,
		      0, 0, 0, 0 }
		}
	};
	
	//构造方法
	 MoveCanvas(){
		 if(bDebug == false)
		 {
			 blockType=(int)(Math.random()*100)%7;
		 }
		 else
		 {
			 blockType=4;
		 }
	    rotateState=(int)(Math.random()*100)%4;
	    newblock();
		newmap();
		setwall();
		setSize((column+2)*30,row*30+1);
		isReborn=0;
		flagStart=1;
		c=new Color((int)(Math.random()*1000)%256,(int)(Math.random()*1000)%256,(int)(Math.random()*1000)%256);
	}
	
	//初始化地图
	public void newmap() {
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < column; j++) {
				map[i][j] = 0;
			}
		}
	}
	
	//设置围墙
	public void setwall(){
		for(int i=0;i<row;i++){
			map[i][0]=2;
			map[i][column-1]=2;
		}
		for(int j=0;j<column;j++){
			map[row-1][j]=2;
		}
	}
	
	//生成新方块
	public void newblock(){
		if(bDebug == false)
		 {
			 blockType1=(int)(Math.random()*100)%7;
		 }
		 else
		 {
			 blockType1=4;
		 }
		rotateState1=(int)(Math.random()*100)%4;
		//debug();
		x=150;
		y=0;
		c1=new Color((int)(Math.random()*1000)%256,(int)(Math.random()*1000)%256,(int)(Math.random()*1000)%256);
	}
	
	//paint
	public void paint(Graphics g){
		super.paint(g);
		//画当前方块
		for(int i=0;i<16;i++){
			if(shapes[blockType][rotateState][i]==1){
				g.setColor(c);
				g.fillRect((i%4)*30+x,(i/4)*30+y,30,30);
			}
		}
		//已经固定的方块和墙
		g.setColor(Color.black);
		for(int i=0;i<row;i++){
			for(int j=0;j<column;j++){
				if(map[i][j]==2){
					g.drawRect(j*30,i*30,30,30);
				}
				if(map[i][j]==1){
					g.setColor(new Color(128,0,128));
					g.fillRect(j*30, i*30, 30, 30);
				}
			}
		}
		//画下一个方块
		g.setColor(c1);
		if(flagStart==1){
			for(int i=0;i<16;i++){
				if(shapes[blockType1][rotateState1][i]==1){
					g.fillRect(13*30+(i%4)*30,10*30+(i/4)*30,30,30);
				}
			}
		}
	}
	
    //掉落
	public void down(){
		if(canmove(x,y+30,rotateState)){
			y=y+30;
		}
		else{
			changemap(x,y,rotateState);
			//消行
			delline();
		}
		if(isReborn==1){
			newblock();
			isReborn=0;
		}
		repaint();
	}
	
	//往左
	public void left(){
		if(canmove(x-30,y,rotateState))
			x=x-30;
		repaint();
	}
	
	//往右
	public void right(){
		if(canmove(x+30,y,rotateState))
			x=x+30;
		repaint();
	}
	//旋转
	public void rotate(){
		int t=(rotateState+1)%4;
		if(canmove(x,y,t))
			rotateState=t;
		repaint();
	}
	//边界限制以及生成新方块
	public boolean canmove(int x,int y,int r){
		for(int i=0;i<16;i++){
			if(shapes[blockType][r][i]==1)
				if(map[((i/4)*30+y)/30][((i%4)*30+x)/30]==2||map[((i/4)*30+y)/30][((i%4)*30+x)/30]==1)	
					return false;
		}
		return true;
	}
	//落下方块固定为背景
	public void changemap(int x,int y,int r){
		//如果背景为0，则固定为背景
		for(int i=0;i<16;i++)
			for(int j=0;j<16;j++){
				if(shapes[blockType][r][j]==1)
					map[((j/4)*30+y)/30][((j%4)*30+x)/30]=1;
			}
		isReborn=1;
		//把下一个方块赋给当前方块
		blockType=blockType1;
		rotateState=rotateState1;
		c=c1;
	}
	public void delline() {
		int Mark = 0;
		for (int indexrow = row-1; indexrow >0; indexrow--) 
		{
			for (int indexcolumn=column-1; indexcolumn >0; indexcolumn--) 
			{
				if (map[indexrow][indexcolumn] == 1)
				{
					Mark = Mark + 1;
				}
				if (Mark == column-2)
				{
					for(int DeleteBox = column-1; DeleteBox >0; DeleteBox--)
					{
						map[indexrow][DeleteBox]=0;
					}
					for (int DownRow = indexrow;DownRow > 0; DownRow--) 
					{
						for (int DownCol = column-1; DownCol >0; DownCol--) 
						{
							if(DownRow-1 >=0)
							{
								map[DownRow][DownCol] = map[DownRow-1][DownCol];
							}
						}
					}
					score+=10;
				}
			}
		Mark = 0;
		}
	}
	//游戏结束
	public boolean gameOver(){
		for(int i=5;i<7;i++)
			if(map[0][i]==1){
				JOptionPane.showMessageDialog(null, "很遗憾，游戏结束！");
				return true;
			}
		return false;
	}
	
	//调试模式
	/*public void debug(){
		blockType=4;
	}*/
}