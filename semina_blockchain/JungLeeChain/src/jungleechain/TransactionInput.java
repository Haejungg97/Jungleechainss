package jungleechain;

public class TransactionInput {
	public String transactionOutputId; // transactionOutputs -> transactionId에 대한 참조
	public TransactionOutput UTXO; // 보내지지 않은 트랜젝션 output를 포함한다.
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
	
}
