# 需要opencv-python包
# 鼠标左键选点，鼠标右键结束一条线
# 左键终端会显示选的点的坐标，右键结束画线时，终端会显示包含当前画的所有线坐标的三维数组

import numpy as np
import cv2

img1=cv2.imread('./src/main/resources/assets/luotianyi/icon.png')

# 图像长宽
height, width = img1.shape[:2]

# 画坐标轴，以图片中心为原点，宽度的一半记为1
start_point = (int(width / 2) , 0)
end_point = (int(width / 2), int(height))
cv2.line(img1, start_point, end_point, (0, 255, 0), 1)
start_point = (0 , int(height / 2))
end_point = (width, int(height / 2))
cv2.line(img1, start_point, end_point, (0, 255, 0), 1)

#像素点坐标初定义
lines = []
pro_x = []
pro_y = []
last_x = 0
last_y = 0
#定义鼠标点击事件并将点击坐标输入数组
def mouse_img_cod(event, cod_x, cod_y, flags, param):
    global pro_x, pro_y, lines, last_x, last_y
    if event == cv2.EVENT_LBUTTONDOWN:  
        if(len(pro_x)) :    
            cv2.line(img1, (last_x, last_y), (cod_x, cod_y), (0, 255, 0), 1)    # 连线
        val_x = (cod_x - width / 2) / (width / 2)
        val_y = - (cod_y - height / 2) / (width / 2)
        xy = '%.3f,%.3f' % (val_x,val_y)
        cv2.circle(img1, (cod_x, cod_y), 1, (0, 255, 0), thickness = int(2 * width / 800))  # 画坐标点
        # cv2.putText(img1, xy, (cod_x, cod_y), cv2.FONT_HERSHEY_DUPLEX, 0.3 * width / 800, (255, 0, 0), thickness = int(1 * width / 800)) #将坐标值放在图片内
        val_x = format(val_x, '.3f')
        val_y = format(val_y, '.3f')
        pro_x.append(val_x)
        pro_y.append(val_y)
        last_x = cod_x
        last_y = cod_y
        print(val_x, val_y)
        cv2.imshow('image', img1)
    if event == cv2.EVENT_RBUTTONDOWN:
        line = []
        for i in range(0, len(pro_x)) :
            line.append([pro_x[i], pro_y[i]])
        lines.append(line)
        pro_x = []
        pro_y = []
        print(str(lines).replace('[', '{').replace(']', '}').replace('\'', ''))

cv2.namedWindow('image', cv2.WINDOW_AUTOSIZE) #创建一个名为image的窗口
cv2.setMouseCallback('image', mouse_img_cod) #鼠标事件回调
cv2.imshow('image',img1) #显示图片
cv2.waitKey(0) #按下任意键退出窗口
cv2.destroyAllWindows()

