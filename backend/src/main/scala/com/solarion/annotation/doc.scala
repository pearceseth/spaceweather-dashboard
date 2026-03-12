package com.solarion.annotation

import scala.annotation.StaticAnnotation

/**
 * Links this definition to its documentation file within the project.
 * Path is relative to the project root.
 *
 * Used by the documentation update agent to build a reliable
 * source-to-doc mapping without heuristic inference.
 *
 * @param path Path to the documentation file relative to project root
 */
class doc(path: String) extends StaticAnnotation
