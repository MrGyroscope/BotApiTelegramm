import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;




public class Cuckoo_bot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update){
        if (update.hasMessage() && update.getMessage().hasText()){
            String message_text = update.getMessage().getText();
            String chat_id = update.getMessage().getChatId().toString();
            SendMessage message= new SendMessage();
            System.out.println(message_text); //логирую в консоль, что вводили пользователи
            String[] Recipe=GetRecipeFromApi(message_text);
            String msg;
            for (int i=1;i<10;i++) { //выводим массив с рецептами
                msg = Recipe[i];
                message.setText(msg);
                message.setChatId(chat_id);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public String getBotUsername(){
        return "qubotrainbot";
    }
    @Override
    public String getBotToken(){
        return "";
    } //токен бота

    public String[] GetRecipeFromApi (String Coctail) {
        String query = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=";      //сайт, к которому обращаемся
        String NotFind="Коктейль не найден. Проверьте правильность написания. Пример: Bloody Mary"+"\n"+"P.S. Я пока понимаю только рецепты на английском";
        String[] FinalRecipe = new String[10];
        HttpsURLConnection connection = null;//определяем подключение, изначально обнуляем
        try {
            connection=(HttpsURLConnection) new URL(query+Coctail).openConnection(); //определяем адрес URL
            connection.setRequestMethod("GET"); //определяем метод запроса
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-AGENT", "Mozilla/5.0");
            connection.connect(); //само подключение

            StringBuilder sb= new StringBuilder(); //конструктор для длинных строк

            //настройки подключения
            if(HttpsURLConnection.HTTP_OK == connection.getResponseCode()){ //проверяем, если 200, значит запрос успешно выполнился
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); // и тогда считываем ответ и передаем считанное, код для чтения киррилицы

                String line;
                while ((line = in.readLine()) != null){ //проверяем, что вычитанная строка не null
                    sb.append(line); //добавляем к вычитанному результату строку
                }
            } else {
                System.out.println("fail" + connection.getResponseCode() + "," + connection.getResponseMessage()); //если код ответа не 200
            }

            //Парсим ответ и засовываем в стрингу
            Object RecipeCoctail = new JSONParser().parse(String.valueOf(sb)); //Считываем Json
            JSONObject cock = (JSONObject) RecipeCoctail; //Кастим в JSONObject
            JSONArray Drink = (JSONArray) cock.get("drinks"); //Засовываем в массив

            if (Drink == null){
                FinalRecipe[1]=NotFind;
            }
            else
            {
                Iterator DrinkRe = Drink.iterator(); //Создаем коллекцию
                int m=1;
                while (DrinkRe.hasNext()){ //Перебираем коллекцию
                    JSONObject text = (JSONObject) DrinkRe.next();
                    FinalRecipe[m]=FinalRecipe[m]+text.get("strDrink")+"\n";
                    FinalRecipe[m]=FinalRecipe[m]+"Alcoholic: "+text.get("strAlcoholic")+"\n";
                    FinalRecipe[m]=FinalRecipe[m]+"Glass: "+text.get("strGlass")+"\n";
                    FinalRecipe[m]=FinalRecipe[m]+"Ingredient: ";
                    int n=0;
                    while (++n < 16){ //Заводим счетчик, чтобы избавиться от null значений
                        if (text.get("strIngredient"+n) != null)
                        {
                            FinalRecipe[m]=FinalRecipe[m]+" +"+text.get("strIngredient"+n);
                            if (text.get("strMeasure"+n) != null) {
                                FinalRecipe[m]=FinalRecipe[m]+"("+text.get("strMeasure"+n)+")";
                            }
                        }
                    }
                    FinalRecipe[m]=FinalRecipe[m]+"\n"+"Recipe: "+text.get("strInstructions")+"\n";
                    FinalRecipe[m]=FinalRecipe[m]+"Picture: "+text.get("strDrinkThumb")+"\n"+"\n";
//                    System.out.println(FinalRecipe[m]);
                    m=m+1;
                }
            }

        } catch (Throwable cause) {         //ловим ошибки
            cause.printStackTrace();
        } finally {                         //вне зависимости от ошибки проверяем
            if (connection != null)           //что было создано подключение
                connection.disconnect();      //обрываем подключение
        }
        return FinalRecipe;
    }

}
