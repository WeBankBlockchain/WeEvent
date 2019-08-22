package main
import(
	"fmt"
	"encoding/json"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

type TopicController struct {
}

type TopicInfo struct {
	timepStamp string
	version  string
}

var topicContractName string = ""
var topicMap = make(map[string]TopicInfo)

func (t *TopicController) Init(stub shim.ChaincodeStubInterface) pb.Response{
    fmt.Println(" << ====[Init] success init it is view in docker ======")
    return shim.Success([]byte("success init"))
}

func (t *TopicController) Invoke(stub shim.ChaincodeStubInterface) pb.Response{
    fn, args := stub.GetFunctionAndParameters()
	switch fn {
		case "addTopicContractName":
			return t.addTopicContractName(stub, args)
		case "updateTopicContractName":
			return t.updateTopicContractName(stub, args)
		case "getTopicContractName":
			return t.getTopicContractName(stub, args)
		case "addTopicInfo":
			return t.addTopicInfo(stub, args)
		case "getTopicInfo":
			return t.getTopicInfo(stub, args)
	}

	return shim.Error("invoke func error")
}

func (t *TopicController) addTopicContractName(stub shim.ChaincodeStubInterface,args[] string) pb.Response{
	if(topicContractName == ""){
		topicContractName=args[0]
		return shim.Success([]byte("setTopicName success"))
	}
	return shim.Error("topicContractName exist")
}

func (t *TopicController) updateTopicContractName(stub shim.ChaincodeStubInterface,args[] string) pb.Response{
	topicContractName=args[0]
	return shim.Success([]byte("updateTopicContractName success"))
}

func (t *TopicController) getTopicContractName(stub shim.ChaincodeStubInterface,args[] string) pb.Response{
	return shim.Success([]byte(topicContractName))
}

func (t *TopicController) addTopicInfo(stub shim.ChaincodeStubInterface,args[] string) pb.Response{
	var topicInfo TopicInfo
	topicInfo.timepStamp = args[1]
	topicInfo.version = args[2]
	topicMap[args[0]] = topicInfo;//args[0]:topicName args[1]:timestamp args[2]:verison
	return shim.Success([]byte("addTopicInfo success"))
}

func (t *TopicController) getTopicInfo(stub shim.ChaincodeStubInterface,args[] string) pb.Response{
	jsonTopicInfo, err := json.Marshal(topicMap[args[0]])
	if err != nil{
		return shim.Error("getTopicInfo err")
	}
	return shim.Success([]byte(string(jsonTopicInfo)))
}

func main(){
    err := shim.Start(new(TopicController))
    if err != nil{
        fmt.Println("Error starting Simple chaincode : %s",err)
    }
}