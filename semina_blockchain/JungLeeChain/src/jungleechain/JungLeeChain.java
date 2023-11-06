package jungleechain;

import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

public class JungLeeChain {
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	// HashMap 을 가져오면 키를 통해 값을 찾을 수 있다.
	public static int difficulty = 5; // 테스트를 위해 difficulty 5로 설정
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;
	
	public static void main(String[] args) {
		// Bouncey castle을 보안 공급자로 설정한다
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		// 새로운 지갑만들기
		walletA = new Wallet();
		walletB = new Wallet();
		Wallet coinbase = new Wallet();
		
		// 100개의 JungLeeCoin을 walletA에 보내는 genesis 트랜젝션을 만들기
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);	 //genesis 트랜젝션에 수동적으로 서명	
		genesisTransaction.transactionId = "0"; //트랜젝션 id를 수동적으로 설정
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId)); 
		//트랜젝션 output을 추가하기
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); 
		// 중요! UTXOs 리스트에 우리의 첫번째 트랜젝션을 저장하기
		
		System.out.println("Genesis 블록을 생성하고 채굴하는 중... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		
		//테스트하기
		Block block1 = new Block(genesis.hash);
		System.out.println("\nWalletA의 잔액은 : " + walletA.getBalance());
		System.out.println("\nWalletA에서 WalletB로 (40)을 송금시도합니다...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA의 잔액은: " + walletA.getBalance());
		System.out.println("WalletB의 잔액은: " + walletB.getBalance());
		
		Block block2 = new Block(block1.hash);
		System.out.println("\nWalletA 에서 가지고 있는 것보다 많은 금액 (1000)을 보내려고 합니다...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA의 잔액은: " + walletA.getBalance());
		System.out.println("WalletB의 잔액은: " + walletB.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("\nWalletB에서 WalletA로 (20)을 송금시도합니다...");
		block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
		System.out.println("\nWalletA의 잔액은: " + walletA.getBalance());
		System.out.println("WalletB의 잔액은: " + walletB.getBalance());
		
		isChainValid();

		
		
}
//		Block genesisBlock = new Block("안녕하세요, 저는 첫번째 블록입니다!", "0");
//		System.out.println("첫번째 블록의 해시 :  " + genesisBlock.hash);
		
//		Block secondBlock = new Block("안녕하세요, 저는 두번째 블록입니다!", genesisBlock.hash);
//		System.out.println("두번째 블록의 해시 :  " + secondBlock.hash);
		
//		Block thirdBlock = new Block("안녕하세요, 저는 세번째 블록입니다!", secondBlock.hash);
//		System.out.println("세번째 블록의 해시 :  " + thirdBlock.hash);
		
//	}	
	// 블록체인 배열리스트에 블록 추가하기
//	blockchain.add(new Block ("안녕 나는 첫번째 블록이야", "0"));
//	System.out.println("첫번째 블록을 채굴 중입니다...");
//	blockchain.get(0).mineBlock(difficulty);
//	
//	blockchain.add(new Block ("헬로~ 나는 두번째 블록이야", blockchain.get(blockchain.size()-1).hash));
//	System.out.println("두번째 블록을 채굴 중입니다...");
//	blockchain.get(1).mineBlock(difficulty);
//	
//	blockchain.add(new Block ("안녕하세요? 저는 세번째 블록입니다.", blockchain.get(blockchain.size()-1).hash));
//	System.out.println("세번째 블록을 채굴 중입니다...");
//	blockchain.get(2).mineBlock(difficulty);
//	
//	System.out.println("\nBlockchain is Valid: " + isChainValid());
//	
//	String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
//	System.out.println("\nThe block chain: ");
//	System.out.println(blockchainJson);
//	}
	
	
//	// public 과 private keys 테스트하기
//	System.out.println("Private and public keys : ");
//	System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
//	System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
//	
//	//walletA에서 WalletB 로 가는 테스트 트랜젝션 만들기 
//	Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
//	transaction.generateSignature(walletA.privateKey);
//	
//	//서명이 작동되는지 확인하고 public key에서 확인하기
//	System.out.println("서명 확인됨");
//	System.out.println(transaction.verifiySignature());	
	
	
		//체인의 모든 블록에서 루프를 돌며 해시들을 비교하는 메서드를 만든다.
		// 각 해시 변수가 실제로 계산된 해시와 같고, 
		// 이전 블록의 해시가 previousHash 변수와 같은지 확인하기 위함이다.
		
public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); 
		//주어진 블럭 상태에서 하나의 사용되지 않은 트랜젝션들의 일시적인 작업 리스트
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		
			// 반복 루프 만들기
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			// 등록된 해시와 계산된 해시 비교하기
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ) {
					System.out.println("현재 해시가 같지 않음.");
					return false;					
			}
			//이전 해시와 등록된 이전 해시를 비교하기
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
					System.out.println("이전 해시와 같지 않음.");
					return false;
			}
				//해시가 해결되었을 때
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
					System.out.println("해당 블록은 채굴되지 않음.");
					return false;
			}
			
			// 블록체인 트랜젝션을 통과하는 루프
			TransactionOutput tempOutput;
			for(int t=0; t < currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifiySignature()) {
					System.out.println("Transaction(" + t + ")에서의 서명이 잘못되었습니다.");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs이 Transaction(" + t + ")의 outputs과 일치합니다.");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Transaction(" + t + ")과 관련된 input이 없습니다.");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Transaction(" + t + ")과 관련된 input이 잘못되었습니다.");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.recipient) {
					System.out.println("#Transaction(" + t + ") output의 수신자가 아닙니다.");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output의 'change'은 발신자가 아닙니다.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain이 유효합니다.");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
}
		
