package br.edu.utfpr;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SalesReader {

    private final List<Sale> sales;

    public SalesReader(String salesFile) {

        final var dataStream = ClassLoader.getSystemResourceAsStream(salesFile);

        if (dataStream == null) {
            throw new IllegalStateException("File not found or is empty");
        }

        final var builder = new CsvToBeanBuilder<Sale>(new InputStreamReader(dataStream, StandardCharsets.UTF_8));

        sales = builder
                .withType(Sale.class)
                .withSeparator(';')
                .build()
                .parse();
    }

    public BigDecimal totalOfCompletedSales() {
        var totalValueOfCompletedSales = sales.stream()
                .filter(Sale::isCompleted)
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalValueOfCompletedSales;
    }

    public BigDecimal totalOfCancelledSales() {
        var totalValueOfCancelledSales = sales.stream()
                .filter(Sale::isCancelled)
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalValueOfCancelledSales;
    }

    public Optional<Sale> mostRecentCompletedSale() {
        var mostRecentCompletedSale = sales.stream()
                .filter(Sale::isCompleted)
                .sorted(Comparator.comparing(Sale::getDeliveryDate).reversed())
                .findFirst();
        return mostRecentCompletedSale;
    }

    public long daysBetweenFirstAndLastCancelledSale() {
        var daysBetween = ChronoUnit.DAYS.between(
                sales.stream().filter(Sale::isCancelled).map(Sale::getSaleDate).min(Comparator.naturalOrder()).orElseThrow(),
                sales.stream().filter(Sale::isCancelled).map(Sale::getSaleDate).max(Comparator.naturalOrder()).orElseThrow()
        );
        return daysBetween;
    }

    public BigDecimal totalCompletedSalesBySeller(String sellerName) {
        var totalValueOfCompletedSalesBySeller = sales.stream()
                .filter(Sale::isCompleted)
                .filter(sale -> sale.getSeller().equals(sellerName))
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalValueOfCompletedSalesBySeller;
    }

    public long countAllSalesByManager(String managerName) {
        var allSalesByManager = sales.stream()
                .filter(sale -> sale.getManager().equals(managerName))
                .count();
        return allSalesByManager;
    }

    public BigDecimal totalSalesByStatusAndMonth(Sale.Status status, Month... months) {
        var totalSalesByStatusAndMonth = sales.stream()
                .filter(sale -> sale.getStatus().equals(status))
                .filter(sale -> Stream.of(months)
                        .anyMatch(month -> month
                                .equals(sale.getSaleDate().getMonth())))
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalSalesByStatusAndMonth;
    }

    public Map<String, Long> countCompletedSalesByDepartment() {
        var countCompletedSalesByDepartment = sales.stream()
                .filter(Sale::isCompleted)
                .collect(Collectors.groupingBy(
                        Sale::getDepartment,
                        Collectors.counting()
                ));
        return countCompletedSalesByDepartment;
    }

    public Map<Integer, Map<String, Long>> countCompletedSalesByPaymentMethodAndGroupingByYear() {
        var countCompletedSalesByPaymentMethodAndGroupingByYear = sales.stream()
                .filter(Sale::isCompleted)
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().getYear(),
                        Collectors.groupingBy(
                                Sale::getPaymentMethod,
                                Collectors.counting()
                        )
                ));
        return countCompletedSalesByPaymentMethodAndGroupingByYear;
    }

    public Map<String, BigDecimal> top3BestSellers() {
        return sales.stream()
                .filter(Sale::isCompleted)
                .collect(Collectors.groupingBy(
                        Sale::getSeller,
                        Collectors.reducing(BigDecimal.ZERO, Sale::getValue, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


}
