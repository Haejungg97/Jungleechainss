package jungleechain;

import java.util.ArrayList;
import java.util.Date;

public class Block {
	
	public String hash; // 디지털 시그니처가 담길 것
	public String previousHash; // 이전 블록의 해시를 담을 것
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//	private String data; //우리의 메시지를 넣을 예정
	private long timeStamp;
	
	private int nonce; // calculateHash() 메서드와 mindBlock() 메서드에 넣을 
					   // int 타입의 변수
	
	// 블록 생성자
//	public Block(String data, String previousHash) {
	public Block(String previousHash) {
//		this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash(); //-> 주의! 다른 값을 설정한 후에 이 메서드를 추가해야 한다!
	}
	// 각 블록의 내용에 따라 새로운 해시를 계산
	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256(
				previousHash + 
				Long.toString(timeStamp) + 
				Integer.toString(nonce) +
//				data
				merkleRoot
				);
		return calculatedhash;
	}
	
	//채굴하기 - 해시 target에 도달할 때까지 nonce 값을 올리기 	
	// mindBlock()은 difficulty라는 int를 취한다.(해결해야하는 0의 수들)
	// 1이나 2와 같이 낮은 자리의 difficulty는 대부분 컴퓨터에서 바로 처리될 수 있다.
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDificultyString(difficulty);
		// difficulty가 있는 문자열 만들기
//		String target = new String(new char[difficulty]).replace('\0', '0');
		while(!hash.substring(0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
		}
		System.out.println("야호! 블록이 성공적으로 채굴되었습니다! : " + hash);
	}
	
	
	// 트랜젝션을 이 블록에 추가하기
	public boolean addTransaction(Transaction transaction) {
		//d
		if(transaction == null) return false;
		if((previousHash != "0")) {
			if((transaction.processTransaction() != true)) {
				System.out.println("트랜젝션 처리 실패. 폐기됨.");
				return false;
			}
		}
		transactions.add(transaction);
		System.out.println("트랜젝션이 성공적으로 블록에 추가됨");
		return true;
	}
	
	
}