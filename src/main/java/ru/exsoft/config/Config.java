package ru.exsoft.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {
    public int width = 640;                                     //ширина захвата камеры
    public int height = 480;                                    //высота захвата камеры
    public int sensitivity = 10;                                //порог отсечения бинарных шумов, чувствительность
    public double minArea = 10.0D;                              //минимальная площадь контура, дял регистрации обнаружения
    public int framesTrigger = 5;                               //колво кадров, после которых начнётся запись
    public long endRecordSec = 5;                               //Время в секундах, которое будет записанно с момента последнего движения, и максимальный промежуток между ними
    public String tempVideoPatch = "./video.mp4";               //Путь сохранения видео
    public String tempImagePatch = "./img.jpg";                 //Путь сохранения изображений
    public boolean rotate = false;                              //Переворачиваем ли изображение
    public double angle = 180;                                  //Угол, на который требуется повернуть изображение
    public int waitSeconds = 5;                                 //Сколько секунд следует подождать перед началом детектирования
    public int filterPeriod = 20;                               //Число фильтрованных проверок ухода пользователя
    public HashMap<String, String> knownHosts = new HashMap<>();//Мап для известных устройств (для нотификации ухода\прихода) (MAC - User)
    public List<String> greenHosts = new ArrayList<>();         //Лист для "своих" устройств (MAC)
}
