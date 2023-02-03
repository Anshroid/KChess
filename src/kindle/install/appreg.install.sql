INSERT OR IGNORE INTO "handlerIds" VALUES('uk.co.anshroid.kchess');
INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','lipcId','uk.co.anshroid.kchess');
INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','jar','/opt/amazon/ebook/booklet/kchess_booklet.jar');

INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','maxUnloadTime','45');
INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','maxGoTime','60');
INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','maxPauseTime','60');

INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','default-chrome-style','NH');
INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','unloadPolicy','unloadOnPause');
INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','extend-start','Y');
INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','searchbar-mode','transient');
INSERT OR IGNORE INTO "properties" VALUES('uk.co.anshroid.kchess','supportedOrientation','U');

INSERT OR IGNORE INTO "mimetypes" VALUES('kchess','MT:image/x.kchess');
INSERT OR IGNORE INTO "extenstions" VALUES('kchess','MT:image/x.kchess');
INSERT OR IGNORE INTO "properties" VALUES('archive.displaytags.mimetypes','image/x.kchess','KChess');
INSERT OR IGNORE INTO "associations" VALUES('com.lab126.generic.extractor','extractor','GL:*.kchess','true');
INSERT OR IGNORE INTO "associations" VALUES('uk.co.anshroid.kchess','application','MT:image/x.kchess','true');
