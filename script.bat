adb shell run-as com.sharifian.shaheen.foodwatch chmod 777 /data/data/com.sharifian.shaheen.foodwatch/files

adb shell run-as com.sharifian.shaheen.foodwatch chmod 777 /data/data/com.sharifian.shaheen.foodwatch/files/food.cblite

adb shell cp /data/data/com.sharifian.shaheen.foodwatch/files/food.cblite /sdcard/

adb pull /sdcard/food.cblite