package gRpcExample;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;

public class HelloWorldClient {
	private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

	private final ManagedChannel channel;
	private final GreeterGrpc.GreeterBlockingStub blockingStub; // 동기식 호출 스텁

	// 서버 주소와 포트로 클라이언트 초기화
	public HelloWorldClient(String host, int port) {
		// ManagedChannel: 서버와의 연결을 관리 (Plaintext 사용 - 실제 환경에서는 TLS 권장)
		channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
		// blockingStub: 서버 메서드를 동기적으로 호출할 수 있는 스텁 생성
		blockingStub = GreeterGrpc.newBlockingStub(channel);
	}

	// 클라이언트 종료 시 채널 닫기
	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	// 서버의 SayHello 메서드 호출
	public void greet(String name) {
		logger.info("Will try to greet " + name + " ...");
		HelloRequest request = HelloRequest.newBuilder().setName(name).build();
		HelloReply response;
		try {
			// blockingStub을 사용하여 RPC 호출
			response = blockingStub.sayHello(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Greeting: " + response.getMessage());
	}

	public static void main(String[] args) throws Exception {
		// 서버 주소와 포트 설정
		HelloWorldClient client = new HelloWorldClient("localhost", 50051);
		try {
			String user = "world";
			if (args.length > 0) {
				user = args[0]; // 커맨드 라인 인자가 있으면 사용
			}
			// 서버 메서드 호출
			client.greet(user);
		} finally {
			// 클라이언트 종료
			client.shutdown();
		}
	}
}