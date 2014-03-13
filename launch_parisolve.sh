PROJECT_NAME="parisolve"
VM_OPTIONS=""
case `uname -m` in
  'x86_64')
    BIT_NO='64'
  ;;
  *)
    BIT_NO='32'
  ;;
esac

case "$OSTYPE" in
  'darwin*')
    OS_NAME='mac'
	VM_OPTIONS="$VM_OPTIONS -XstartOnFirstThread"
  ;;
  'linux-gnu')
    OS_NAME='linux'
  ;;
  'msys')
    OS_NAME='win'
  ;;
  *)
    echo "unknown system"
    exit
  ;;
esac

java -jar $VM_OPTIONS ${PROJECT_NAME}_$OS_NAME$BIT_NO.jar
