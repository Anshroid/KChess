DELETE FROM "handlerIds" WHERE handlerId='uk.co.anshroid.kchess';
DELETE FROM "properties" WHERE handlerId='uk.co.anshroid.kchess';
DELETE FROM "associations" WHERE handlerId='uk.co.anshroid.kchess';

DELETE FROM "mimetypes" WHERE ext='kchess';
DELETE FROM "extenstions" WHERE ext='kchess';
DELETE FROM "properties" WHERE value='KChess';
DELETE FROM "associations" WHERE contentId='GL:*.kchess';
