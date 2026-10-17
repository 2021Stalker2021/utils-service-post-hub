#!/usr/bin/env bash
#!/usr/bin/env bash
# !!! НЕ УДАЛЯТЬ ЭТУ СТРОКУ !!!
# Она говорит Linux: "Запускай этот файл через программу bash"
# Без неё Docker контейнер не запустится (ошибка permission denied)

# получает имя профиля из переменной окружения
PROFILE=${PROFILE:-local-idea}

# Показывает в консоли какой профиль запускается
echo "Starting service with profile: $PROFILE"

# Запускаем наше приложение
exec java -jar /srv/utils-service-1.0-SNAPSHOT.jar --spring.profiles.active=$PROFILE