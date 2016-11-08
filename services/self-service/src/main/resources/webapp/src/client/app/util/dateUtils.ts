export class DateUtils {
  public static diffBetweenDatesInHours(dateString : number) : number {
    let currentDate = new Date();
    let creationDate = new Date(dateString);

    let hourDifference = 0;
    hourDifference = Math.round(Math.abs(currentDate.getTime() - creationDate.getTime()) / 36e5);

    return hourDifference;
  }
}
