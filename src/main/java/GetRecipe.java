import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

public class GetRecipe {
    public String GetRecipeFromApi (String Coctail) {
            String query = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=";      //сайт, к которому обращаемся
            String NotFind="\"Recipe not found. Please check the spelling is correct. P.S. I only understand recipes in English\" \n"+"\"Рецепт не найден. Пожалуйста, проверьте правильность написания. П.С. Я пока понимаю только рецепты на английском\"";
            String FinalRecipe = new String();
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
                Iterator DrinkRe = Drink.iterator(); //Создаем коллекцию

                if (cock.get("drinks").toString() == null){
                    FinalRecipe=NotFind;
                }
                else
                {
                    while (DrinkRe.hasNext()){ //Перебираем коллекцию
                        JSONObject text = (JSONObject) DrinkRe.next();
                        FinalRecipe=FinalRecipe+"Name: "+text.get("strDrink")+"\n";
                        FinalRecipe=FinalRecipe+"Alcoholic: "+text.get("strAlcoholic")+"\n";
                        FinalRecipe=FinalRecipe+"Glass: "+text.get("strGlass")+"\n";
                        FinalRecipe=FinalRecipe+"Ingredient: ";
                        int n=0;
                        while (++n < 16){ //Заводим счетчик, чтобы избавиться от null значений
                            if (text.get("strIngredient"+n) != null)
                            {
                                FinalRecipe=FinalRecipe+" +"+text.get("strIngredient"+n);
                                if (text.get("strMeasure"+n) != null) {
                                    FinalRecipe=FinalRecipe+"("+text.get("strMeasure"+n)+")";
                                }
                            }
                        }
                        FinalRecipe=FinalRecipe+"\n"+"Recipe: "+text.get("strInstructions")+"\n";
                        FinalRecipe=FinalRecipe+"Picture: "+text.get("strDrinkThumb")+"\n"+"\n";
                    }
                }
//                System.out.println(FinalRecipe);
            } catch (Throwable cause) {         //ловим ошибки
                cause.printStackTrace();
            } finally {                         //вне зависимости от ошибки проверяем
                if (connection != null)           //что было создано подключение
                    connection.disconnect();      //обрываем подключение
            }
            return FinalRecipe;
    }
}
