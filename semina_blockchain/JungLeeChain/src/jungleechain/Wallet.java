package jungleechain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {

	public PrivateKey privateKey; // 트랜젝션에 서명할 때 사용 -> 비밀. 소유자만 알고 있어야 한다.
	public PublicKey publicKey; // 주소 역할, 무결성을 확인하는 데 사용 -> 지불받기 위해 다른사람들에게 공유 가능
	
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	// 이 지갑이 소유한 UTXOs만
	
	public Wallet() {
		generateKeyPair();
	}
	
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// key generator 초기화 후 KeyPair 메서드 생성
			keyGen.initialize(ecSpec, random); // 256바이트는 수용 가능한 보안 수준을 제공
			KeyPair keyPair = keyGen.generateKeyPair();

			// KeyPair에서 public key와 private key 설정
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
			
		}catch(Exception e) {
				throw new RuntimeException(e);
		}
	}
	
	
	// 잔액(balance)를 반환하고 이 지갑이 소유한 UTXO를 this.UTXOs에 저장한다.
	public float getBalance() {
		float total = 0;
	for(Map.Entry<String,TransactionOutput> item: JungLeeChain.UTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
		if(UTXO.isMine(publicKey)) { //만약 output(코인)이 나에게 있다면,
			UTXOs.put(UTXO.id,UTXO); // '사용되지않은 트랜젝션 리스트'에 추가하기
			total += UTXO.value;
		}
	}
			return total;
	}

	//이 지갑에서 새로운 트랜젝션은 만들고 반환
	public Transaction sendFunds(PublicKey _recipient, float value) {
			if(getBalance() < value) {
				System.out.println("#트랜젝션을 보낼 돈이 충분하지 않음. 폐기된 거래.");
				return null;
	}
		// inputs 배열 리스트 만들기
	ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
			
	float total = 0;
	for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()) {
		TransactionOutput UTXO = item.getValue();
		total += UTXO.value;
		inputs.add(new TransactionInput(UTXO.id));
		if(total > value) break;
	}
			
	Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
	newTransaction.generateSignature(privateKey);
			
	for(TransactionInput input: inputs) {
		UTXOs.remove(input.transactionOutputId);
	}
	return newTransaction;
		
}
	
}
