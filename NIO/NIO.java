import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * 
 */
public class NIO {

	/**
	 * 一、缓冲区（Buffer）：在 Java NIO 中负责数据的存取。缓冲区就是数组。用于存储不同数据类型的数据
	 * 
	 * 根据数据类型不同（boolean 除外），提供了相应类型的缓冲区：
	 * ByteBuffer !!
	 * CharBuffer
	 * ShortBuffer
	 * IntBuffer
	 * LongBuffer
	 * FloatBuffer
	 * DoubleBuffer
	 * 
	 * 上述缓冲区的管理方式几乎一致，通过 allocate() 获取缓冲区
	 * 
	 * 二、缓冲区存取数据的两个核心方法：
	 * put() : 存入数据到缓冲区中
	 * get() : 获取缓冲区中的数据
	 * 
	 * 三、缓冲区中的四个核心属性：
	 * capacity : 容量，表示缓冲区中最大存储数据的容量。一旦声明不能改变。
	 * limit : 界限，表示缓冲区中可以操作数据的大小。（limit 后数据不能进行读写）
	 * position : 位置，表示缓冲区中正在操作数据的位置。
	 * 
	 * mark : 标记，表示记录当前 position 的位置。可以通过 reset() 恢复到 mark 的位置
	 * 
	 * 0 <= mark <= position <= limit <= capacity
	 * 
	 * 四、直接缓冲区与非直接缓冲区：
	 * 非直接缓冲区：通过 allocate() 方法分配缓冲区，将缓冲区建立在 JVM 的内存中
	 * 直接缓冲区：通过 allocateDirect() 方法分配直接缓冲区，将缓冲区建立在物理内存中。可以提高效率
	 */	
	public void testBuffer(){	
	}
	/**
	 * Buffer 的取放值和各个属性
	 */
	@Test
	public void testBuffer1(){	
		//1.通过 allocate() 分配一个指定大小的缓冲区
		ByteBuffer buf = ByteBuffer.allocate(1024);
		System.out.println("-----------------allocate()----------------");
		System.out.println(buf.position());
		System.out.println(buf.limit());
		System.out.println(buf.capacity());
		
		//2.利用put() 存入数据到缓冲区
		String str = "abcdef";
		buf.put(str.getBytes());
		System.out.println("-----------------put()----------------");
		System.out.println(buf.position());
		System.out.println(buf.limit());
		System.out.println(buf.capacity());
		//3. 切换读取数据模式
		buf.flip();
		System.out.println("-----------------flip()----------------");
		System.out.println(buf.position());
		System.out.println(buf.limit());
		System.out.println(buf.capacity());
		//4. 利用 get() 读取缓冲区中的数据
		byte[] dst = new byte[buf.limit()];
		buf.get(dst);
		System.out.println(new String(dst, 0, dst.length));
		
		System.out.println("-----------------get()----------------");
		System.out.println(buf.position());
		System.out.println(buf.limit());
		System.out.println(buf.capacity());
		//5. rewind() : 可重复读
		buf.rewind();
		System.out.println("-----------------rewind()----------------");
		System.out.println(buf.position());
		System.out.println(buf.limit());
		System.out.println(buf.capacity());
		//6. clear() : 清空缓冲区. 但是缓冲区中的数据依然存在，但是处于“被遗忘”状态
		buf.clear();
		System.out.println("-----------------clear()----------------");
		System.out.println(buf.position());
		System.out.println(buf.limit());
		System.out.println(buf.capacity());
		System.out.println((char)buf.get());
	}
	
	/**
	 * Buffer mark()和reset()
	 */
	@Test
	public void testBuffer2(){
		String str = "abcde";
		ByteBuffer buf = ByteBuffer.allocate(1024);
		buf.put(str.getBytes());
		buf.flip();
		byte[] dst = new byte[buf.limit()];
		buf.get(dst, 0, 2);
		
		System.out.println(new String(dst, 0, 2));
		System.out.println("-----------------position() : 当前位置----------------");
		System.out.println(buf.position());
		
		System.out.println("-----------------mark() : 标记----------------");
		buf.mark();
		
		buf.get(dst, 2, 2);
		System.out.println(new String(dst, 2, 2));
		System.out.println(buf.position());
		
		System.out.println("-----------------reset() : 恢复到 mark 的位置----------------");
		buf.reset();
		System.out.println(buf.position());
		
		//判断缓冲区中是否还有剩余数据
		if(buf.hasRemaining()){
			//判断缓冲区中是否还有剩余数据
			System.out.println(buf.remaining());
		}	
	}
	
	/**
	 * 直接缓冲区与非直接缓冲区
	 */
	@Test
	public void testBuffer3(){
		ByteBuffer buf = ByteBuffer.allocate(1024);
		System.out.println(buf.isDirect());	
		ByteBuffer dirBuf = ByteBuffer.allocateDirect(1024);
		System.out.println(dirBuf.isDirect());
	}
	
	/**
	 * 一、通道（Channel）：用于源节点与目标节点的连接。在 Java NIO 中负责缓冲区中数据的传输。Channel 本身不存储数据，因此需要配合缓冲区进行传输。
	 * 
	 * 二、通道的主要实现类
	 * 	java.nio.channels.Channel 接口：
	 * 		|--FileChannel
	 * 		|--SocketChannel
	 * 		|--ServerSocketChannel
	 * 		|--DatagramChannel
	 * 
	 * 三、获取通道
	 * 1. Java 针对支持通道的类提供了 getChannel() 方法
	 * 		本地 IO：
	 * 		FileInputStream/FileOutputStream
	 * 		RandomAccessFile
	 * 
	 * 		网络IO：
	 * 		Socket
	 * 		ServerSocket
	 * 		DatagramSocket
	 * 		
	 * 2. 在 JDK 1.7 中的 NIO.2 针对各个通道提供了静态方法 open()
	 * 3. 在 JDK 1.7 中的 NIO.2 的 Files 工具类的 newByteChannel()
	 * 
	 * 四、通道之间的数据传输
	 * transferFrom()
	 * transferTo()
	 * 
	 * 五、分散(Scatter)与聚集(Gather)
	 * 分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中
	 * 聚集写入（Gathering Writes）：将多个缓冲区中的数据聚集到通道中
	 * 
	 * 六、字符集：Charset
	 * 编码：字符串 -> 字节数组
	 * 解码：字节数组  -> 字符串
	 * 
	 */
	public void testChannel(){		
	}
	/**
	 * 1.针对支持通道的类提供了 getChannel() 方法
	 * 利用通道完成文件的复制（非直接缓冲区）
	 */
	@Test
	public void testChannel1(){	
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		//1. Java 针对支持通道的类提供了 getChannel() 方法 , 
		//①获取通道
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		
		try {
			fis = new FileInputStream("1.jpg");
			fos = new FileOutputStream("2.jpg");
			inChannel = fis.getChannel();
			outChannel = fos.getChannel();
			//②分配指定大小的缓冲区
			ByteBuffer buf = ByteBuffer.allocate(1024);
			//③将通道中的数据存入缓冲区中
			while(inChannel.read(buf) != -1){
				//切换读取数据的模式
				buf.flip();
				//④将缓冲区中的数据写入通道中
				outChannel.write(buf);
				//清空缓冲区
				buf.clear();
			}
			System.out.println("aaa");
		} catch (IOException e) {		
			e.printStackTrace();
		} finally {
			if(outChannel != null){
				try {
					outChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(inChannel != null){
				try {
					inChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	/**
	 * 2. NIO.2 针对各个通道提供了静态方法 open()
	 * 使用直接缓冲区完成文件的复制(内存映射文件)
	 * @throws IOException 
	 */
	@Test
	public void testChannel2() throws IOException{
		FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), 
				                                 StandardOpenOption.READ);
		FileChannel outChannel = FileChannel.open(Paths.get("3.jpg"),
				                                 StandardOpenOption.READ, 
				                                 StandardOpenOption.WRITE,
				                                 StandardOpenOption.CREATE);	
		//内存映射文件
		MappedByteBuffer inMappedBuf = inChannel.map(MapMode.READ_ONLY, 0, inChannel.size());
		MappedByteBuffer outMappedBuf = outChannel.map(MapMode.READ_WRITE, 0, inChannel.size());
		
		//直接对缓冲区进行数据的读写操作
		byte[] dst = new byte[inMappedBuf.limit()];
		inMappedBuf.get(dst);
		outMappedBuf.put(dst);
		
		inChannel.close();
		outChannel.close();	
	}
	/**
	 * 3.通道之间的数据传输(直接缓冲区) ----简单易行 推荐！
	 * transferFrom()
	 * transferTo()
	 */
	@Test
	public void testChannel3() throws IOException{
		FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), 
                                                 StandardOpenOption.READ);
		FileChannel outChannel1 = FileChannel.open(Paths.get("4.jpg"),
				                                 StandardOpenOption.READ, 
				                                 StandardOpenOption.WRITE,
				                                 StandardOpenOption.CREATE);
		FileChannel outChannel2 = FileChannel.open(Paths.get("5.jpg"),
                StandardOpenOption.READ, 
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
		//方式一:transferFrom()
		outChannel1.transferFrom(inChannel, 0, inChannel.size());
		//方式二:transferTo()
		inChannel.transferTo(0, inChannel.size(), outChannel2);
		
		inChannel.close();
		outChannel1.close();
		outChannel2.close();
	}

	/**
	 * 4、分散(Scatter)与聚集(Gather)
	 * 分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中
	 * 聚集写入（Gathering Writes）：将多个缓冲区中的数据聚集到通道中
	 * @throws Exception 
	 */
	@Test
	public void testChannel4() throws Exception{
        RandomAccessFile raf1 = new RandomAccessFile("1.txt", "rw");	
		//1. 获取通道
		FileChannel channel1 = raf1.getChannel();	
		//2. 分配指定大小的缓冲区
		ByteBuffer buf1 = ByteBuffer.allocate(100);
		ByteBuffer buf2 = ByteBuffer.allocate(1024);	
		//3. 分散读取
		ByteBuffer[] bufs = {buf1, buf2};
		channel1.read(bufs);
		
		for (ByteBuffer byteBuffer : bufs) {
			byteBuffer.flip();
		}
		
		System.out.println(new String(bufs[0].array(), 0, bufs[0].limit()));
		System.out.println("-----------------");
		System.out.println(new String(bufs[1].array(), 0, bufs[1].limit()));
		
		//4. 聚集写入
		RandomAccessFile raf2 = new RandomAccessFile("2.txt", "rw");
		FileChannel channel2 = raf2.getChannel();
		
		channel2.write(bufs);		
	}
	/**
	 * 查看有多少字符集
	 */
	@Test
	public void testChannel5(){
		Map<String, Charset> map = Charset.availableCharsets();
		Set<Map.Entry<String, Charset>> set = map.entrySet();
		
		for (Map.Entry<String, Charset> entry : set) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
	}
	/**
	 *字符集
	 * @throws CharacterCodingException 
	 */
	@Test
	public void testChannel6() throws CharacterCodingException{
		Charset cs1 = Charset.forName("GBK");
		
		//获取编码器
		CharsetEncoder ce = cs1.newEncoder();
		
		//获取解码器
		CharsetDecoder cd = cs1.newDecoder();
		
		CharBuffer cBuf = CharBuffer.allocate(1024);
		cBuf.put("fighting");
		cBuf.flip();
		//编码
		ByteBuffer bBuf = ce.encode(cBuf);
		for (int i = 0; i < 12; i++) {
			System.out.println(bBuf.get());
		}
		
		//解码
		bBuf.flip();
		CharBuffer cBuf2 = cd.decode(bBuf);
		System.out.println(cBuf2.toString());
		
        System.out.println("------------------------------------------------------");
		
		Charset cs2 = Charset.forName("UTF-8");//乱码
		bBuf.flip();
		CharBuffer cBuf3 = cs2.decode(bBuf);
		System.out.println(cBuf3.toString());
	}	
}
