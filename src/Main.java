import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

// Clase principal
public class Main {
    public static void main(String[] args) {
        CurrencyConverter converter = new CurrencyConverter();
        Scanner scanner = new Scanner(System.in);

        // URL de la nueva API de Open Exchange Rates
        String apiUrl = "https://openexchangerates.org/api/latest.json?app_id=f1e9df2b079b436982ecf1852134a9f2";

        // Construir la solicitud
        HttpRequest request = converter.buildRequest(apiUrl);

        try {
            // Enviar la solicitud y obtener la respuesta
            String response = converter.sendRequest(request);

            // Analizar la respuesta JSON y ejecutar el menú
            converter.analyzeJsonResponse(response, scanner);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Clase para convertir monedas
class CurrencyConverter {
    private HttpClient httpClient;
    private List<String> conversionHistory = new ArrayList<>();
    private JsonObject rates; // Guardar las tasas de cambio para ser utilizadas en la conversión

    // Constructor
    public CurrencyConverter() {
        httpClient = HttpClient.newHttpClient();
    }

    // Construir la solicitud HTTP
    public HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    }

    // Enviar la solicitud y obtener la respuesta
    public String sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body(); // Retornar el cuerpo de la respuesta si es exitoso
        } else {
            return "Error: " + response.statusCode(); // Mostrar el código de error si no es exitoso
        }
    }

    // Método para analizar la respuesta JSON
    public void analyzeJsonResponse(String jsonResponse, Scanner scanner) {
        // Parsear el JSON
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        rates = jsonObject.getAsJsonObject("rates");  // Guardar las tasas de cambio

        // Menú interactivo
        boolean exit = false;
        while (!exit) {
            System.out.println("\nMenú de conversión de monedas:");
            System.out.println("1. Elegir monedas y convertir");
            System.out.println("2. Mostrar tasas de cambio");
            System.out.println("3. Mostrar historial de conversiones");
            System.out.println("4. Salir");
            System.out.print("Elige una opción: ");

            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    handleCurrencySelectionAndConversion(scanner);
                    break;
                case 2:
                    showExchangeRates();
                    break;
                case 3:
                    displayHistory();
                    break;
                case 4:
                    exit = true;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    // Método para manejar la selección de monedas y la conversión
    private void handleCurrencySelectionAndConversion(Scanner scanner) {
        Set<String> availableCurrencies = rates.keySet();

        // Mostrar las monedas disponibles
        System.out.println("Monedas disponibles: ");
        List<String> currencyList = new ArrayList<>(availableCurrencies);
        for (int i = 0; i < currencyList.size(); i++) {
            System.out.println((i + 1) + ". " + currencyList.get(i));
        }

        // Seleccionar moneda de origen
        System.out.print("Elige la moneda de origen (número): ");
        int fromCurrencyIndex = scanner.nextInt() - 1;
        String fromCurrency = currencyList.get(fromCurrencyIndex);
        double fromRate = rates.get(fromCurrency).getAsDouble();

        // Seleccionar moneda de destino
        System.out.print("Elige la moneda de destino (número): ");
        int toCurrencyIndex = scanner.nextInt() - 1;
        String toCurrency = currencyList.get(toCurrencyIndex);
        double toRate = rates.get(toCurrency).getAsDouble();

        // Obtener el monto a convertir
        System.out.print("Introduce la cantidad en " + fromCurrency + ": ");
        double amountInFromCurrency = scanner.nextDouble();

        // Realizar la conversión
        double amountInToCurrency = convertCurrency(amountInFromCurrency, fromRate, toRate);
        System.out.println(amountInFromCurrency + " " + fromCurrency + " en " + toCurrency + ": " + amountInToCurrency);

        // Guardar en el historial
        addToHistory(amountInFromCurrency + " " + fromCurrency + " en " + toCurrency + ": " + amountInToCurrency);
    }

    // Método para convertir monedas
    public double convertCurrency(double amount, double fromRate, double toRate) {
        return (amount / fromRate) * toRate;
    }

    // Método para agregar al historial
    private void addToHistory(String record) {
        String timestamp = LocalDateTime.now().toString();
        conversionHistory.add(record + " | Fecha y hora: " + timestamp);
    }

    // Método para mostrar el historial de conversiones
    private void displayHistory() {
        System.out.println("Historial de conversiones:");
        for (String record : conversionHistory) {
            System.out.println(record);
        }
    }

    // Método para mostrar las tasas de cambio actuales
    private void showExchangeRates() {
        System.out.println("\nTasas de cambio actuales (en USD):");
        for (String currency : rates.keySet()) {
            double rate = rates.get(currency).getAsDouble();
            System.out.println(currency + ": " + rate);
        }
    }
}
