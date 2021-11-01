import java.io.*;
import java.util.*;
import java.util.Map.Entry;

//Node class to store all the nodes in huffman tree
class Node
{
	Character ch;
	int count;
	Node left;
	Node right;
	BitSet huffmanCode = new BitSet();
	int noOfBits = 0;
	public Node(Character ch,int count,Node left,Node right)
	{
		this.ch = ch;
		this.count = count;
		this.left = left;
		this.right = right;
	}
	
	public String toString()
	{
		String codeStr = "";
		for (int i=0; i<noOfBits; i++)
			if (huffmanCode.get(i) == true)
				codeStr = "1" + codeStr;
			else
				codeStr = "0" + codeStr;
		
		return ch + "-" + count + "-" +codeStr;
	}
}

//Comparator to compare two nodes by comparing the count's of each characters
class NodeComparator implements Comparator<Node>
{

	@Override
	public int compare(Node o1, Node o2) {
		
		return o1.count - o2.count;
	}
	
	
}
public class Huffman
{
	//To convert a symbol or character into huffmancode
	public static void encodeNode (Node node)
	{
		if (node.left != null)
		{
			//Move all the bits to left by one position
			for (int i=0;i<node.noOfBits; i++)
				node.left.huffmanCode.set(i+1, node.huffmanCode.get(i));
			node.left.huffmanCode.set(0, false);
			node.left.noOfBits = node.noOfBits + 1;
			encodeNode (node.left);
		}
		if (node.right != null)
		{
			//Move all the bits to left by one position
			for (int i=0;i<node.noOfBits; i++)
				node.right.huffmanCode.set(i+1, node.huffmanCode.get(i));
			node.right.huffmanCode.set(0, true);
			node.right.noOfBits = node.noOfBits +1;
			encodeNode(node.right);
		}
	}
	
	//To convert huffmancode into a symbol or character
	public static Node decodeData(Node node, BitSet encodedData, int index)
	{
		if (node.left == null && node.right == null)
		{
			return node;
		}
		if (encodedData.get(index) == true)
		{
			return decodeData (node.right, encodedData, index+1);
		}
		else
		{
			return decodeData(node.left, encodedData, index+1);
		}
	}
	
	
	public static void main(String [] agrs)  throws Exception
	{
		//Reading the data file and counting how many times a character is repeated
		File file = new File("/Users/harshith/eclipse-workspace/Huffman/data/A Tale of Two Cities.txt");
		FileReader reader = new FileReader(file);
		char chArr[] = new char[10];
		int noOfCharsRead = reader.read(chArr);
		HashMap<Character, Integer> charMap = new HashMap<Character, Integer>();
		while (noOfCharsRead != -1)
		{
			for (int i =0; i<noOfCharsRead; i++)
			{
				if (charMap.containsKey(chArr[i]))
				{
					//CHaracter already exists. Increment the count
					int count = charMap.get(chArr[i]);
					count++;
					charMap.put(chArr[i], count);
				}
				else
				{
					//First occurance. COunt is one
					charMap.put(chArr[i], 1);
				}
			}
			noOfCharsRead = reader.read(chArr);
		}
		reader.close();
		
		//Display the counts
		System.out.println(charMap);
		System.out.println("Count is: " + charMap.size());
		
		//Converting the characters into nodes to be put in a binary tree. PriorityQueue will sort the nodes
		PriorityQueue<Node> queue = new PriorityQueue<>(new NodeComparator());
		HashMap<Character, Node> leafNodes = new HashMap<Character, Node>();
		for (Entry<Character, Integer> entry: charMap.entrySet())
		{
			char ch = entry.getKey();
			int count = entry.getValue();
			Node node = new Node(ch, count, null, null);
			queue.add(node);
			leafNodes.put(ch, node);
		}
		System.out.println(queue);
		System.out.println ("Size: " +queue.size());

		//Construct the huffman binary tree
		while (queue.size() != 1)
		{
			Node node1 = queue.poll();
			Node node2 = queue.poll();
			int sum = node1.count + node2.count;
			Node parent = new Node(null, sum, node2, node1);
			queue.add(parent);
		}
		System.out.println(queue);
		System.out.println ("Size: " +queue.size());
		
		//Get the top root node
		Node rootNode = queue.poll();
		
		//Calculate the huffmancodes for the leaf nodes in the binary tree
		encodeNode(rootNode);
		System.out.println ("Huffman codes");
		for (Entry<Character, Node> entry: leafNodes.entrySet())
		{
			System.out.println(entry.getValue());
		}
		
		//Encode the data in the file using huffman codes from the leaf nodes		
		reader = new FileReader(file);
		noOfCharsRead = reader.read(chArr);
		BitSet encodedData = new BitSet();
		int encodedBitPos = 0;
		while (noOfCharsRead != -1)
		{
			for (int i =0; i<noOfCharsRead; i++)
			{
				char ch = chArr[i];
				Node symbolNode = leafNodes.get(ch);
				for (int bitNo=symbolNode.noOfBits-1; bitNo>=0 ; bitNo--)
				{
					encodedData.set(encodedBitPos, symbolNode.huffmanCode.get(bitNo));
					encodedBitPos++;
				}
			}
			noOfCharsRead = reader.read(chArr);
		}
		reader.close();
		
		//Store the encoded data in a file
		byte[] encodedBytes = encodedData.toByteArray();
		FileOutputStream encodedOutput = new FileOutputStream("/Users/harshith/eclipse-workspace/Huffman/data/encoded.txt");
		encodedOutput.write(encodedBytes);
		encodedOutput.close();
		
		
		//Decode the data from encoding file
		//Read encoded data and construct the bit array
		FileInputStream encodedInput = new FileInputStream("/Users/harshith/eclipse-workspace/Huffman/data/encoded.txt");
		encodedBytes = new byte[10];
		int noOfBytesRead = 0;
		noOfBytesRead = encodedInput.read(encodedBytes);
		encodedData = new BitSet();
		encodedBitPos = 0;
		while (noOfBytesRead != -1)
		{
			BitSet readData = BitSet.valueOf(encodedBytes);
			for (int i=0; i<noOfBytesRead * 8; i++)
			{
				encodedData.set(encodedBitPos, readData.get(i));
				encodedBitPos++;
			}
			noOfBytesRead = encodedInput.read(encodedBytes);
		}
		encodedInput.close();
		
		//Open file for storing decoded data
		FileWriter decodedData = new FileWriter("/Users/harshith/eclipse-workspace/Huffman/data/decoded.txt");
		
		//Go through all the bits and decode
		System.out.println("Converted Data");
		int bitIndex = 0;
		while (bitIndex < encodedBitPos)
		{
			Node symbolNode = decodeData(rootNode, encodedData, bitIndex);
			//System.out.print(symbolNode.ch);
			decodedData.write(symbolNode.ch);
			bitIndex += symbolNode.noOfBits;
		}
		decodedData.close();
	}
}