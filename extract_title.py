import cv2
import numpy as np
from pythonRLSA import rlsa
import math
import pytesseract
from PIL import Image


def extract_title(img):
        image = cv2.imread(img)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        (thresh, binary) = cv2.threshold(gray, 100, 200, cv2.THRESH_BINARY | cv2.THRESH_OTSU)
        # cv2.imshow('binary', binary)
        cv2.imwrite('binary.png', binary)
        (contours, _) = cv2.findContours(~binary, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        # find contours
        for contour in contours:
            [x,y,w,h] = cv2.boundingRect(contour)
            cv2.rectangle(image, (x,y), (x+w,y+h), (0, 200, 0), 1)
        # cv2.imshow('contour', image)
        cv2.imwrite('contours.png', image)
        # cv2.waitKey(0)
        # cv2.destroyAllWindows()

        mask = np.ones(image.shape[:2], dtype="uint8") * 200
        (contours, _) = cv2.findContours(~binary,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
        heights = [cv2.boundingRect(contour)[3] for contour in contours]
        avgheight = sum(heights)/len(heights)
        for c in contours:
            [x,y,w,h] = cv2.boundingRect(c)
            if h > 2*avgheight:
                cv2.drawContours(mask, [c], -1, 0, -1)
        # cv2.imshow('filter', mask)
        cv2.imwrite('filter.png', mask)

        x, y = mask.shape
        value = max(math.ceil(x/100),math.ceil(y/100))+20 #heuristic
        mask = rlsa.rlsa(mask, True, False, value) #rlsa application
        # cv2.imshow('rlsah', mask)
        cv2.imwrite('rlsah.png', mask)
        (contours, _) = cv2.findContours(~mask,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE) # find contours
        mask2 = np.ones(image.shape, dtype="uint8") * 200 # blank 3 layer image
        for contour in contours:
            [x, y, w, h] = cv2.boundingRect(contour)
            if w > 0.60*image.shape[1]: # width heuristic applied
                title = image[y: y+h, x: x+w]
                mask2[y: y+h, x: x+w] = title # copied title contour onto the blank image
                image[y: y+h, x: x+w] = 200 # nullified the title contour on original image
        # cv2.imshow('title', mask2)
        cv2.imwrite('title.png', mask2)
        # cv2.imshow('content', image)
        # cv2.imshow('content.png', image)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        title = pytesseract.image_to_string(Image.fromarray(mask2))
        # print(title)
        # title = title.split(" ")
        # print(title)
        return title
