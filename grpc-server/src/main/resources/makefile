hello:
	protoc --proto_path=proto --java_out=../java \
  --plugin=protoc-gen-grpc-java=protoc-gen-grpc-java.exe --grpc-java_out=../java proto/hello/hello.proto

###
user:
	protoc --proto_path=proto --java_out=../java \
  --plugin=protoc-gen-grpc-java=protoc-gen-grpc-java.exe --grpc-java_out=../java proto/user/user.proto