package jungleechain;

import java.security.*;
import java.util.ArrayList;

import jungleechain.JungLeeChain;

public class Transaction {

		public String transactionId; // 트랜젝션의 해시
		public PublicKey sender; // 발신자 주소 /public key
		public PublicKey recipient; // 수신자 주소 /public key
		public float value;
		public byte[] signature; // 다른사람이 우리 지갑에서 돈을 쓰는 것을 방지

		public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
		
		private static int sequence = 0; // 생성된 트랜젝션의 대략적인 갯수
		
		//생성자
		public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
			this.sender = from;
			this.recipient = to;
			this.value = value;
			this.inputs = inputs;
			
		}
		

		
		//새로운 트랜젝션이 만들어질 수 있으면 true를 반환
		public boolean processTransaction() {
			if(verifiySignature() == false) {
				System.out.println("#트랜젝션 서명 확인 실패");
				return false;
			}
			
			//트랜젝션 inputs 모으기 (사용되지 않았는지 반드시 확인)
			for(TransactionInput i : inputs) {
				i.UTXO = JungLeeChain.UTXOs.get(i.transactionOutputId);
			}
			
			//트랜젝션이 유효한지 확인
			if(getInputsValue() < JungLeeChain.minimumTransaction) {
				System.out.println("#트랜젝션 input이 너무 작음: "+getInputsValue());
				System.out.println(JungLeeChain.minimumTransaction+"보다 큰 양을 입력하세요.");
				return false;
			}
			
			
			//트랜젝션 outputs 만들기
			float leftOver = getInputsValue() - value; // inputs의 값을 받고 남은 것을 변경
			transactionId = calculateHash();
			outputs.add(new TransactionOutput(this.recipient, value, transactionId)); 
			// 값을 수신자에게 전달
			
			outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));
			// 남은 것을 다시 발신자에게 전달
			
			//outputs을 사용되지않은(보내지지않은) 리스트에 추가
			for(TransactionOutput o : outputs) {
				JungLeeChain.UTXOs.put(o.id, o);
			}
			
			//UTXO 리스트에 보내진 트랜젝션 inputs을 제거
			for(TransactionInput i : inputs) {
				if(i.UTXO == null) continue; // 만약 트랜젝션을 찾을 수 없다면 스킵하기
				JungLeeChain.UTXOs.remove(i.UTXO.id);
			}
			
			return true;
		}
		
	//inputs 값의 총합을 반환하는 메서드
		public float getInputsValue() {
			float total = 0;
			for(TransactionInput i: inputs) {
				if(i.UTXO == null) continue; // 만약 트랜젝션을 찾을 수 없다면 스킵하기
				total += i.UTXO.value;
			}
			return total;
		}
		
		//변조를 원하지 않는 모든 데이터에 서명
		public void generateSignature(PrivateKey privateKey) {
			String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
			signature = StringUtil.applyECDSASig(privateKey,data);		
		}
		
		//서명한 데이터가 변조되지 않았는지 확인
		public boolean verifiySignature() {
			String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
			return StringUtil.verifyECDSASig(sender, data, signature);
		}
		
		
		//outputs 값의 총합을 반환하는 메서드
		public float getOutputsValue() {
			float total = 0;
			for(TransactionOutput o: outputs) {
				total += o.value;
			}
			return total;
		}		
		
		// 트랜젝션 해시(id로 사용될)를 계산
		private String calculateHash() {
				sequence++; // 
				return StringUtil.applySha256(
						StringUtil.getStringFromKey(sender) +
						StringUtil.getStringFromKey(recipient) +
						Float.toString(value) + sequence
						);
		}		
}
