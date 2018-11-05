# RfIdTool приложение для чтения и записи NFC меток формата nfca

Приложение является "служебным" и не имеет основной activity, также нет в меню приложений android.
Запуск происходит из других приложений, туда же передаются считанные данные или передаются данные для записи в метку.

Пример использование в android приложении:

    protected void nfcStart(boolean read, String readedId) {
        
        if (read)) {
            Intent intent = new Intent("com.ploal.rfidtool.NFCREAD");
            intent.putExtra("IdLabel", readedId); //множественное чтение, предыдущий id метки 
        }
        else{
            Intent intent = new Intent("com.ploal.rfidtool.NFCWRITE");
            intent.putExtra("PageNumber", PageNumber); //глоб. переменная номер страницы 
            intent.putExtra("WriteString", WriteString); //глоб. переменная текст для записи
        }
        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data) {
            String event = data.getStringExtra("event");
            String uid = data.getStringExtra("uid");
            String result = data.getStringExtra("result");
            String text = data.getStringExtra("text");
            String[] techArr = data.getStringArrayExtra("tech");
            //обработка полученных данных ...
        }
    }

Пример использование в мобильном приложении(клиенте) 1с:
    
    &НаКлиенте
    Процедура ЗапускПриложенияNFC(Чтение=Истина,ПрочитанныйID="")
        ЗПМУ = Новый ЗапускПриложенияМобильногоУстройства();
            
        Если Чтение Тогда
            ЗПМУ.Действие = "com.ploal.rfidtool.NFCREAD";
            ЗПМУ.ДополнительныеДанные.Добавить("IdLabel",ПрочитанныйID);
        Иначе
            ЗПМУ.Действие = "com.ploal.rfidtool.NFCWRITE";
            ЗПМУ.ДополнительныеДанные.Добавить("PageNumber",""+НомерСтраницы);
            ЗПМУ.ДополнительныеДанные.Добавить("WriteString",ТекстЗаписи);
        КонецЕсли;
        
        Если ЗПМУ.ПоддерживаетсяЗапуск() Тогда
            ЗПМУ.Запустить(Истина);	
            Событие = "";
            Для Каждого Стр Из ЗПМУ.ДополнительныеДанные Цикл
                Если Стр.Ключ = "event" Тогда
                    Событие = Стр.Значение;
                ИначеЕсли Стр.Ключ = "uid" Тогда
                    УИД = Стр.Значение;	
                ИначеЕсли Стр.Ключ = "result" Тогда
                    Результат = Стр.Значение; //HEX строка
                ИначеЕсли Стр.Ключ = "text" Тогда
                    Текст = Стр.Значение;	
                ИначеЕсли Стр.Ключ = "tech" Тогда
                    Техлист = Стр.Значение;	
                КонецЕсли;
            КонецЦикла;
            //обработка полученных данных ...
        КонецЕсли;	
    КонецПроцедуры  


Готовый пример использования и собранное приложение можно скачать по ссылке: [https://infostart.ru/public/622737/](https://infostart.ru/public/622737/)