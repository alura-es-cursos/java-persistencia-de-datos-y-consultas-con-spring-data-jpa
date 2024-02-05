package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporada;
import com.aluracursos.screenmatch.model.Serie;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=4fc7c187";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
  private List<Serie> series = new ArrayList<>();
    private SerieRepository repository;
    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void muestraElMenu(){
        var opcion = -1;
        while (opcion != 0){
            var menu = """
              1 - Buscar series 
              2 - Buscar episodios
              3 - Listar series buscadas
              4 - Buscar series por título
              5 - Top 5 series
              6 - Buscar series por categoría
              7 - Filtrar series
              0 - Salir
              """;
            System.out.println(menu);

        opcion = teclado.nextInt();
        teclado.nextLine();

        switch (opcion) {
            case 1:
                buscarSerieWeb();
                break;
            case 2:
                buscarEpisodioPorSerie();
                break;
            case 3:
                listarSeriesBuscadas();
                break;
            case 4:
                buscarSeriePorTitulo();
                break;
            case 5:
                buscarTop5Series();
                break;
            case 6:
                buscarSeriesPorCategoria();
                break;
            case 7:
                filtrarSeriesPorTemporadaYEvaluacion();
                break;
            case 0:
                System.out.println("Cerrando la aplicación...");
                break;
            default:
                System.out.println("Opción inválida");
        }
        }
    }

    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repository.save(serie);
        //datosSeries.add(datos);
        System.out.println(datos);

    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+" )+ API_KEY);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(datos.sinopsis());
        return datos;
    }

    private void buscarEpisodioPorSerie(){
            listarSeriesBuscadas();
            System.out.println("Escriba el nombre de la serie que desea ver");
            var nombreSerie = teclado.nextLine();
            Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nombreSerie);

            if (serie.isPresent()){
                var serieBuscada = serie.get();
                List<DatosTemporada> temporadas = new ArrayList<>();
                for (int i = 1; i <= serieBuscada.getTotalTemporadas(); i++) {
                    var json = consumoApi.obtenerDatos(URL_BASE + serieBuscada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                    DatosTemporada datosTemporada = conversor.obtenerDatos(json, DatosTemporada.class);
                    temporadas.add(datosTemporada);
                }
                temporadas.forEach(System.out::println);
            }
        }

    private void listarSeriesBuscadas() {
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }
    private void buscarSeriePorTitulo() {
        System.out.println("Escriba el nombre de la serie que desea buscar");
        var nombreSerie = teclado.nextLine();
        Optional<Serie> serieBuscada = repository.findByTituloContainingIgnoreCase(nombreSerie);

        if (serieBuscada.isPresent()){
            System.out.println("Datos de la serie: " + serieBuscada.get());
        } else {
            System.out.println("Datos no encontrados");
        }
    }
    public void buscarTop5Series(){
        List<Serie> topSeries = repository.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s -> System.out.println("Serie: " + s.getTitulo() + "Evaluación: " + s.getEvaluacion()));

    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar séries de que categoria/gênero? ");
        var nombreGenero = teclado.nextLine();
        Categoria categoria = Categoria.fromEspanol(nombreGenero);
        List<Serie> seriesPorCategoria = repository.findByGenero(categoria);
        System.out.println("Séries da categoria " + nombreGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    public void filtrarSeriesPorTemporadaYEvaluacion(){
        System.out.println("¿Filtrar séries con cuántas temporadas? ");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("¿Com evaluación apartir de cuál valor? ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();
        List<Serie> filtroSeries = repository.findByTotalTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(totalTemporadas, evaluacion);
        System.out.println("*** Series filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion()));
    }

    // métodos de opciones adicionales
    private void buscarSeriesPorActor() {
        System.out.println("¿Cuál es el nombre del actor que deseas buscar?");
        var nombreActor = teclado.nextLine();
        System.out.println("¿Apartir de cúal evaluación te gustaria filtrar la búsqueda");
        var evaluacion = teclado.nextDouble();
        List<Serie> seriesEncontradas = repository.findByActoresContainingIgnoreCaseAndEvaluacionGreaterThanEqual(nombreActor, evaluacion);
        System.out.println("Series en que " + nombreActor + " trabajó: ");
        seriesEncontradas.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getEvaluacion()));
    }
}

