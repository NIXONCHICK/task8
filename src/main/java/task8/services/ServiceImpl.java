package task8.services;

public class ServiceImpl implements Service {

  @Override
  public double doHardWork(String work, int value) {
    System.out.println("Calculating for work: " + work + ", value: " + value);
    return Math.random() * value;
  }

  @Override
  public String[] processArray(String prefix, int size) {
    System.out.println("Создаём массив размером: " + size);
    String[] array = new String[size];
    for (int i = 0; i < size; i++) {
      array[i] = prefix + "_" + i;
    }
    return array;
  }
}
