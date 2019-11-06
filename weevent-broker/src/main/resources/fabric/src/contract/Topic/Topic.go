package main
import(
    "fmt"
    "strconv"
    "github.com/hyperledger/fabric/core/chaincode/shim"
    pb "github.com/hyperledger/fabric/protos/peer"
)
var sequenceNumber int

//Topic struct
type Topic struct {
    version string
}

//Init function
func (t *Topic) Init(stub shim.ChaincodeStubInterface) pb.Response{
    fmt.Println("<< ====[Topic Init] success init it is view in docker ====== >>")
    return shim.Success([]byte("success init"))
}

//Invoke function
func (t *Topic) Invoke(stub shim.ChaincodeStubInterface) pb.Response{
    fn, args := stub.GetFunctionAndParameters()
    if fn == "publish" {
        return t.publish(stub, args)
    }
    return shim.Error("publish err")
}

func (t *Topic) publish(stub shim.ChaincodeStubInterface,args []string) pb.Response{
    fmt.Println("<< ====[Topic] publish ====== >>")
    sequenceNumber = sequenceNumber + 1
    fmt.Println("topicName:" + args[0] + " contents:" + args[1] + " extensions:" + args[2] + " sequenceNumber:" + strconv.Itoa(sequenceNumber))
    return shim.Success([]byte(strconv.Itoa(sequenceNumber)))
}

func main(){
    err := shim.Start(new(Topic))
    if err != nil{
        fmt.Println(err)
    }
}