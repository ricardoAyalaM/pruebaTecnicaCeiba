package dominio;

import java.util.Calendar;
import java.util.Date;

import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;
import dominio.repositorio.RepositorioProducto;

public class Vendedor {

	public static final String DATOS_OBLIGATORIOS = "El código del producto y el nombre del cliente son requeridos para la generación de la garantía";
	public static final String EL_PRODUCTO_TIENE_GARANTIA = "El producto ya cuenta con una garantia extendida";
	public static final String PRODUCTO_SIN_GARANTIA = "Este producto no cuenta con garantía extendida";
	public static final String VOCALES = "a|A|e|E|i|I|o|O|u|U";
	public static final double PRECIO_BASE_GARANTIA = 500000.0;
	public static final double VEINTE_PORCIENTO = 0.2;
	public static final double DIEZ_PORCIENTO = 0.1;
	public static final int DIAS_GARANTIA_VEINTE_PORCIENTO = 200;
	public static final int DIAS_GARANTIA_DIEZ_PORCIENTO = 100;

	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	public Vendedor(RepositorioProducto repositorioProducto, RepositorioGarantiaExtendida repositorioGarantia) {
		this.repositorioProducto = repositorioProducto;
		this.repositorioGarantia = repositorioGarantia;

	}

	/**
	 * Método encargado de generar una garantía extendida, la cual es registrada en
	 * caso de cumplir con las reglas de negocio
	 * 
	 * @param codigo        {@link String} código del producto al cual se genera la
	 *                      garantía
	 * @param nombreCliente {@link String} nombre del cliente quien compra la
	 *                      garantía
	 */
	public void generarGarantia(String codigo, String nombreCliente) {
		if (esNuloOVacio(codigo) || esNuloOVacio(nombreCliente)) {
			throw new GarantiaExtendidaException(DATOS_OBLIGATORIOS);
		} else if (tieneGarantia(codigo)) {
			throw new GarantiaExtendidaException(EL_PRODUCTO_TIENE_GARANTIA);
		} else if (validarVocales(codigo)) {
			throw new GarantiaExtendidaException(PRODUCTO_SIN_GARANTIA);
		} else {
			registrarGarantiaExtendida(codigo, nombreCliente);
		}
	}

	/**
	 * Método que permite inicializar los datos de la garantía para almacenarla en
	 * base de datos
	 * 
	 * @param codigo        {@link String} código del producto al cual se genera la
	 *                      garantía
	 * @param nombreCliente {@link String} nombre del cliente quien compra la
	 *                      garantía
	 */
	public void registrarGarantiaExtendida(String codigo, String nombreCliente) {
		Producto producto = repositorioProducto.obtenerPorCodigo(codigo);

		double precioProducto = producto.getPrecio();
		double precioGarantia = 0;
		Calendar fecha = Calendar.getInstance();
		Date fechaSolicitudGarantia = new Date();
		fecha.setTime(fechaSolicitudGarantia);
		if (precioProducto > PRECIO_BASE_GARANTIA) {
			precioGarantia = calcularPrecioGarantia(precioProducto, VEINTE_PORCIENTO);
		} else {
			precioGarantia = calcularPrecioGarantia(precioProducto, DIEZ_PORCIENTO);
		}
		Date fechaFinGarantia = calcularFechaGaratia(fecha, precioProducto);
		
		GarantiaExtendida garantia = new GarantiaExtendida(producto, fechaSolicitudGarantia, fechaFinGarantia,
				precioGarantia, nombreCliente);
		repositorioGarantia.agregar(garantia);
	}

	/**
	 * Método que permite calcular el precio de la garantía con base en el precio
	 * del producto y el porcentaje a ser aplicado
	 * 
	 * @param precioProducto {@link Double} precio del producto
	 * @param porcentaje     {@link Float} porcentaje a ser aplicado del precio del
	 *                       producto
	 * @return {@link Double} precio calculado para la garantía
	 */
	public double calcularPrecioGarantia(double precioProducto, double porcentaje) {
		double precioGarantia = precioProducto * porcentaje;
		return precioGarantia;
	}

	/**
	 * Método que permite calcular la fecha de vencimiento de una garantia
	 * 
	 * @param fechaInicial   {@link Calendar} fecha de inicio de la garantía
	 * @param precioProducto {@link Double} Precio del producto
	 * @return {@link Date} fecha de finalización de la garantía
	 */
	public Date calcularFechaGaratia(Calendar fechaInicial, double precioProducto) {
		if (precioProducto > PRECIO_BASE_GARANTIA) {
			int contadorDiasGarantia = 0;
			do {
				if (fechaInicial.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
					contadorDiasGarantia++;
				}
				fechaInicial.add(Calendar.DAY_OF_YEAR, 1);
			} while (contadorDiasGarantia < DIAS_GARANTIA_VEINTE_PORCIENTO);

			if (fechaInicial.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				fechaInicial.add(Calendar.DAY_OF_YEAR, 2);
			}
		} else {
			fechaInicial.add(Calendar.DAY_OF_YEAR, DIAS_GARANTIA_DIEZ_PORCIENTO);
		}

		return fechaInicial.getTime();
	}

	/**
	 * Método que permite validar si un producto cuenta con una garantía
	 * 
	 * @param codigo {@link String} codigo del producto a verificar
	 * @return {@link Boolean} retorna true en caso de que el producto cuente con
	 *         garantía, de lo contrario retorna false
	 */
	public boolean tieneGarantia(String codigo) {
		return repositorioGarantia.obtenerProductoConGarantiaPorCodigo(codigo) != null;
	}

	/**
	 * Método que permite validar si un código contiene 3 vocales
	 * 
	 * @param codigo {@link String} código del producto
	 * @return {@link Boolean} retorna true en caso de que el código contenga 3
	 *         vocales, de lo contrario retorna false
	 */
	public boolean validarVocales(String codigo) {
		int longitudCodigo = codigo.length();
		String codigoAux = codigo.replaceAll(VOCALES, "");
		int longitudCodAux = codigoAux.length();

		int diferencia = longitudCodigo - longitudCodAux;

		return diferencia == 3;
	}

	/**
	 * Método que permite verificar si una cadena es nula o no contiene caracteres
	 * 
	 * @param dato {@link String} cadena a validar
	 * @return true en caso de que sea vacia o nula, false en caso contrario
	 */
	public boolean esNuloOVacio(String dato) {
		return dato == null || dato.isEmpty();
	}

}
