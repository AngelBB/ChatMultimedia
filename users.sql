-- phpMyAdmin SQL Dump
-- version 4.2.11
-- http://www.phpmyadmin.net
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 04-03-2015 a las 09:41:21
-- Versión del servidor: 5.6.21
-- Versión de PHP: 5.6.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de datos: `spotify`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `users`
--

CREATE TABLE IF NOT EXISTS `users` (
`idUSERS` int(11) NOT NULL,
  `LOGIN` varchar(25) COLLATE utf8_spanish2_ci NOT NULL,
  `PASS` varchar(15) COLLATE utf8_spanish2_ci NOT NULL,
  `ESTADO` tinyint(1) NOT NULL DEFAULT '0',
  `IP` varchar(15) COLLATE utf8_spanish2_ci NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish2_ci;

--
-- Volcado de datos para la tabla `users`
--

INSERT INTO `users` (`idUSERS`, `LOGIN`, `PASS`, `ESTADO`, `IP`) VALUES
(1, 'Antonio', 'Antonio', 0, ''),
(2, 'Polo', 'Polo', 0, ''),
(3, 'JoseLuis', 'JoseLuis', 0, ''),
(4, 'Berrios', 'Berrios', 0, ''),
(5, 'Angel', 'Angel', 0, ''),
(6, 'AngelBar', 'AngelBar', 0, ''),
(7, 'Jose', 'Jose', 0, ''),
(8, 'Manu', 'Manu', 0, ''),
(9, 'Sebas', 'Sebas', 0, ''),
(10, 'Pablo', 'Pablo', 0, ''),
(11, 'Abelardo', 'Abelardo', 0, ''),
(12, 'Sergio', 'Sergio', 0, ''),
(13, 'Carlos', 'Carlos', 0, '');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `users`
--
ALTER TABLE `users`
 ADD PRIMARY KEY (`idUSERS`), ADD UNIQUE KEY `idUSERS_UNIQUE` (`idUSERS`), ADD UNIQUE KEY `LOGIN_UNIQUE` (`LOGIN`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `users`
--
ALTER TABLE `users`
MODIFY `idUSERS` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=14;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
