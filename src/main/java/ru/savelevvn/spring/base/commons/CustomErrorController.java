package ru.savelevvn.spring.base.commons;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Пользовательский контроллер обработки ошибок для Spring Boot приложения.
 * Перехватывает все ошибки, возникающие в веб-приложении, и отображает
 * соответствующую страницу ошибки с понятным сообщением для пользователя.
 *
 * @author Savelev Vladimir
 * @version 1.0
 */
@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    /**
     * Обрабатывает все ошибки, перенаправляя на страницу error.
     * Анализирует код статуса ошибки и формирует соответствующее сообщение
     * для отображения пользователю.
     *
     * @param request объект HTTP запроса, содержащий информацию об ошибке
     * @param model объект модели для передачи данных в представление
     * @return имя шаблона страницы ошибки
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Получаем код статуса ошибки из атрибутов запроса
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = "Произошла непредвиденная ошибка";
        String errorTitle = "Ошибка";

        if (statusCode != null) {
            // Логируем информацию о коде статуса ошибки
            logger.info("Обнаружена ошибка HTTP {}: {}", statusCode, request.getRequestURI());

            // Устанавливаем заголовок и сообщение в зависимости от кода статуса ошибки
            switch (statusCode) {
                case 400:
                    errorTitle = "Некорректный запрос";
                    errorMessage = "Ваш запрос содержит недопустимый синтаксис";
                    logger.warn("Ошибка 400 (Bad Request) для URL: {}", request.getRequestURI());
                    break;
                case 401:
                    errorTitle = "Требуется авторизация";
                    errorMessage = "Для доступа к ресурсу требуется аутентификация";
                    logger.warn("Ошибка 401 (Unauthorized) для URL: {}", request.getRequestURI());
                    break;
                case 403:
                    errorTitle = "Доступ запрещен";
                    errorMessage = "У вас нет прав для доступа к этому ресурсу";
                    logger.warn("Ошибка 403 (Forbidden) для URL: {}", request.getRequestURI());
                    break;
                case 404:
                    errorTitle = "Страница не найдена";
                    errorMessage = "Запрашиваемая страница не существует или временно недоступна";
                    logger.info("Ошибка 404 (Not Found) для URL: {}", request.getRequestURI());
                    break;
                case 500:
                    errorTitle = "Внутренняя ошибка сервера";
                    errorMessage = "Что-то пошло не так на нашем сервере. Пожалуйста, попробуйте позже";
                    logger.error("Ошибка 500 (Internal Server Error) для URL: {}", request.getRequestURI());
                    break;
                default:
                    errorTitle = HttpStatus.valueOf(statusCode).getReasonPhrase();
                    logger.info("Другая ошибка HTTP {}: {} для URL: {}",
                            statusCode, errorTitle, request.getRequestURI());
            }

            model.addAttribute("errorCode", statusCode);
        } else {
            // Код статуса не определен, используем значение по умолчанию
            model.addAttribute("errorCode", 500);
            logger.warn("Не удалось определить код статуса ошибки, установлено значение по умолчанию 500");
        }

        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", errorMessage);

        logger.debug("Подготовлены данные для отображения страницы ошибки: код={}, заголовок={}",
                statusCode, errorTitle);

        return "error";
    }
}