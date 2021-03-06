
/**
 *	Interface that all compression suites must implement. That is they must be
 *	able to compress a file and also reverse/decompress that process.
 * 
 *	@author Brian Lavallee
 *	@since 5 November 2015
 *  @author Owen Atrachan
 *  @since December 1, 2016
 */
public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); // or 256
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;
	public static final int HUFF_COUNTS = HUFF_NUMBER | 2;

	public enum Header{TREE_HEADER, COUNT_HEADER};
	public Header myHeader = Header.TREE_HEADER;
	
	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){
	    while (true){
	        int val = in.readBits(BITS_PER_WORD);
	        if (val == -1) break;
	        
	        out.writeBits(BITS_PER_WORD, val);
	    }
	}

	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){
	   
		int id = in.readBits(BITS_PER_INT);
	    if(id!=HUFF_TREE) {
	    		throw new HuffException("fuck you");
	    }
	    HuffNode root = readTreeHeader(in);
	    readCompressedBits(root,in,out);
	}


		
	
	

	
	public HuffNode readTreeHeader(BitInputStream head) {
		
		int bit = head.readBits(1);
		
		if(bit==0) {
			HuffNode left = readTreeHeader(head);
			HuffNode right = readTreeHeader(head);
			return new HuffNode(0,0,left,right);
		}
		else if(bit==PSEUDO_EOF) {
			return new HuffNode(1,PSEUDO_EOF);
		}
		else {
			return new HuffNode(1, head.readBits(BITS_PER_WORD+1));
		}
	}
	
	
	
	public void readCompressedBits(HuffNode root, BitInputStream in, BitOutputStream out) {
		
		HuffNode copy =root;
		int lOrR = in.readBits(1);
		
		while(in.readBits(1)!=PSEUDO_EOF) {
		
		if(lOrR==0) {
			copy = copy.left();
		}
		else if(lOrR==1) {
			copy = copy.right();
			}
		else {
			out.write(root.value());
			copy  = root;
		}
		
		}
	}
	
	
	
	
	public void setHeader(Header header) {
        myHeader = header;
        System.out.println("header set to "+myHeader);
    }
}