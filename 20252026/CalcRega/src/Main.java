double cota(double primCota, double dist, double declive){
    return primCota + ((primCota + dist) * declive);
}

void main() {
    ArrayList<Double> novosDados = new ArrayList<>();
    Scanner sc = new Scanner(new InputStreamReader(System.in));

    while (sc.hasNext()){
        String line = sc.nextLine();
        String[] values = line.split(" ");

        System.out.println(cota(
                Double.parseDouble(values[0]),
                Double.parseDouble(values[1]),
                Double.parseDouble(values[2])
        ));
    }
}
