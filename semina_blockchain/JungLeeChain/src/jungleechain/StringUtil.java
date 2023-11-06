package jungleechain;

import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
// SHA256알고리즘에 접근하기 위해
import java.security.Signature;
import java.util.Base64;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class StringUtil {
	// Sha256를 적용하고 결과를 출력하기
	public static String applySha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer(); // 해시를 16진수로 포함한다. 
			for (int i = 0; i<hash.length; i++) {
				String hex = Integer.toHexString(0xff &hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//ECDSA 트랜젝션을 적용하고 결과를 바이트로 반환
	// applyECDSASig는 발신자 private key와 문자열 입력을 받아 서명하고 바이트 배열을 반환
		public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA","BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			output = realSig;			
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}
//문자열 서명 확인하기
//	verifyECDSASig는 서명, 공개 키 및 문자열 데이터를 가져오고 서명이 유효한 경우 true 또는 false를 반환
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
			try {
				Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
				ecdsaVerify.initVerify(publicKey);
				ecdsaVerify.update(data.getBytes());
				return ecdsaVerify.verify(signature);
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
	
	// 개체를 json 문자열로 변환하는 간단한 도우미
	public static String getJson(Object o) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(o);
	}
	//해시와 비교할 difficulty 문자열 target을 반환 (예: 난이도 5는 "00000"을 반환)
	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}
	
//		getStringFromKey는 모든 키에서 암호화된 문자열을 반환
		public static String getStringFromKey(Key key) {
			return Base64.getEncoder().encodeToString(key.getEncoded());
		}
		
		
		
// 트랜젝션 배열을 고정하고 merkle root를 반환 		
		public static String getMerkleRoot(ArrayList<Transaction> transactions) {
			int count = transactions.size();
			ArrayList<String> previousTreeLayer = new ArrayList<String>();
			for(Transaction transaction : transactions) {
				previousTreeLayer.add(transaction.transactionId);
			}
			ArrayList<String> treeLayer = previousTreeLayer;
			while(count > 1) {
				treeLayer = new ArrayList<String>();
				for(int i=1; i < previousTreeLayer.size(); i++) {
					treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
				}
				count = treeLayer.size();
				previousTreeLayer = treeLayer;
			}
			String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
			return merkleRoot;
		}							
}