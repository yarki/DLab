export class ErrorMapUtils {
  public static setErrorMessage(errorCode): string {
    if(errorCode)
      var defaultStatus = 'Error status [' + errorCode.status + ']. ';
      var errorMessage = defaultStatus.concat(errorCode.statusText);

      var statusErrorMap = {
        '400': 'Server understood the request, but request content was invalid.',
        '401': 'Unauthorized access.',
        '403': 'Forbidden resource can`t be accessed.',
        '500': 'Internal server error.',
        '503': 'Service unavailable.'
      };

      if(statusErrorMap[errorCode.status])
        errorMessage = defaultStatus.concat(statusErrorMap[errorCode.status]);

      return errorMessage;
  }
}
