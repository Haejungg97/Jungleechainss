package jungleechain;

import java.security.PublicKey;

public class TransactionOutput {
	public String id;
	public PublicKey reciepient; // 코인의 새로운 주인 
	public float value; // 가지고 있는 코인의 양
	public String parentTransactionId; // 이 output이 만든 트랜젝션의 id
	
	// 생성자
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
			this.reciepient = reciepient;
			this.value = value;
			this.parentTransactionId = parentTransactionId;
			this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
	}
	
	//코인이 당신에게 있는지 확인
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
}
