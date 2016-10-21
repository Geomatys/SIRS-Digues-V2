/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.query;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import net.iharder.Base64;
import org.geotoolkit.db.FilterToSQL;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.filter.DefaultPropertyIsLike;
import org.apache.sis.util.ObjectConverters;
import org.geotoolkit.feature.type.AttributeDescriptor;
import org.geotoolkit.feature.type.ComplexType;
import org.geotoolkit.feature.type.GeometryDescriptor;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.AnyInteracts;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.MetBy;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;
import org.opengis.geometry.Envelope;
import org.apache.sis.util.UnconvertibleObjectException;
import org.apache.sis.util.logging.Logging;
import static org.geotoolkit.db.JDBCFeatureStore.JDBC_PROPERTY_RELATION;
import org.geotoolkit.db.reverse.RelationMetaModel;
import org.geotoolkit.feature.type.AssociationType;
import org.geotoolkit.util.NamesExt;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.filter.DefaultPropertyName;
import org.opengis.util.GenericName;

/**
 * Convert filters and expressions in SQL.
 *
 * @author Johann Sorel (Geomatys)
 */
public class SirsFilterToSQL implements FilterToSQL {

    private final ComplexType featureType;
    private int currentsrid;

    public SirsFilterToSQL(ComplexType featureType) {
        this.featureType = featureType;
    }

    ////////////////////////////////////////////////////////////////////////////
    // EXPRESSION EXPRESSION ///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public StringBuilder visit(NilExpression candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        if(true) throw new UnsupportedOperationException("Not supported yet.");
        return sb;
    }

    @Override
    public StringBuilder visit(Add candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        sb.append('(');
        candidate.getExpression1().accept(this, o);
        sb.append(" + ");
        candidate.getExpression2().accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Divide candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        sb.append('(');
        candidate.getExpression1().accept(this, o);
        sb.append(" / ");
        candidate.getExpression2().accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Function candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        if(true) throw new UnsupportedOperationException("Not supported.");
        return sb;
    }

    @Override
    public StringBuilder visit(Literal candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final Object value = candidate.getValue();
        writeValue(sb, value, currentsrid);
        return sb;
    }

    public void writeValue(final StringBuilder sb, Object candidate, int srid){

        if(candidate instanceof Date){
            //convert it to a timestamp, string representation won't be ambiguious like dates toString()
            candidate = new Timestamp(((Date)candidate).getTime());
        }

        if(candidate == null){
          sb.append("NULL");

        }else if(candidate instanceof Boolean){
            sb.append(String.valueOf(candidate));

        }else if(candidate instanceof Double){
            if(((Double)candidate).isNaN()){
                sb.append("'NaN'");
            }else{
                sb.append(String.valueOf(candidate));
            }
        }else if(candidate instanceof Float){
            if(((Float)candidate).isNaN()){
                sb.append("'NaN'");
            }else{
                sb.append(String.valueOf(candidate));
            }
        }else if(candidate instanceof Number){
            sb.append(String.valueOf(candidate));
        }else if(candidate instanceof byte[]){
            //special case for byte array
            sb.append("decode('");
            sb.append(Base64.encodeBytes((byte[])candidate));
            sb.append("','base64')");
        }else if(candidate instanceof Geometry){
            // evaluate the literal and store it for later
            Geometry geom = (Geometry)candidate;

            if(geom.isEmpty()){
                //empty geometries are interpreted as Geometrycollection in postgis < 2
                //this breaks the column geometry type constraint so we replace those by null
                sb.append("NULL");
            }else{

                if(geom instanceof LinearRing){
                    //postgis does not handle linear rings, convert to just a line string
                    geom = geom.getFactory().createLineString(((LinearRing) geom).getCoordinateSequence());
                }
                sb.append("st_geomfromtext('");
                sb.append(geom.toText());
                if(srid>0){
                    sb.append("',").append(srid).append(')');
                }else{
                    sb.append("')");
                }
            }

        }else if(candidate.getClass().isArray()){
            final int size = Array.getLength(candidate);
            sb.append("'{");
            for(int i=0;i<size;i++){
                if(i>0){
                    sb.append(',');
                }
                final Object o = Array.get(candidate, i);
                if(o != null && o.getClass().isArray()){
                    final StringBuilder suba = new StringBuilder();
                    writeValue(suba,o,-1);
                    if(suba.charAt(0)=='\''){
                        sb.append(suba.substring(1, suba.length()-1));
                    }else{
                        sb.append(suba.toString());
                    }
                }else if(!(o instanceof Number || o instanceof Boolean) && o != null){
                    // we don't know what this is, let's convert back to a string
                    String encoding = null;
                    try {
                        encoding = ObjectConverters.convert(o, String.class);
                    } catch (UnconvertibleObjectException e) {
                        Logging.recoverableException(Logging.getLogger("fr.sirs"),SirsFilterToSQL.class, "writeValue", e);
                    }
                    if (encoding == null) {
                        // could not convert back to string, use original value
                        encoding = o.toString();
                    }

                    // single quotes must be escaped to have a valid sql string
                    final String escaped = encoding.replaceAll("'", "''");
                    sb.append('"').append(escaped).append('"');
                }else{
                    writeValue(sb,o,-1);
                }
            }
            sb.append("}'");
        }else{
            // we don't know what this is, let's convert back to a string
            String encoded = null;
            try {
                encoded = ObjectConverters.convert(candidate, String.class);
            } catch (Exception e) {
                Logging.recoverableException(Logging.getLogger("fr.sirs"),SirsFilterToSQL.class, "writeValue", e);
            }
            if (encoded == null) {
                // could not convert back to string, use original value
                encoded = candidate.toString();
            }

            // single quotes must be escaped to have a valid sql string
            final String escaped = encoded.replaceAll("'", "''");
            sb.append('\'');
            sb.append(escaped);
            sb.append('\'');
        }
    }

    @Override
    public StringBuilder visit(Multiply candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        sb.append('(');
        candidate.getExpression1().accept(this, o);
        sb.append(" * ");
        candidate.getExpression2().accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(PropertyName candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final GenericName name = NamesExt.valueOf(candidate.getPropertyName());

        //split the path
        final String str = name.tip().toString();
        int indexOf = str.indexOf('/');
        final String[] parts = str.split("/");

        if(parts.length==1){
            sb.append('"');
            sb.append(name.tip().toString());
            sb.append('"');
        }else{
            //make sub queries

        }

        return sb;
    }

    @Override
    public StringBuilder visit(Subtract candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        sb.append('(');
        candidate.getExpression1().accept(this, o);
        sb.append(" - ");
        candidate.getExpression2().accept(this, o);
        sb.append(')');
        return sb;
    }

    ////////////////////////////////////////////////////////////////////////////
    // FILTER EXPRESSION ///////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public StringBuilder visitNullFilter(Object o) {
        final StringBuilder sb = toStringBuilder(o);
        if(true) throw new UnsupportedOperationException("Not supported.");
        return sb;
    }

    @Override
    public StringBuilder visit(ExcludeFilter candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        sb.append("1=0");
        return sb;
    }

    @Override
    public StringBuilder visit(IncludeFilter candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        sb.append("1=1");
        return sb;
    }

    @Override
    public StringBuilder visit(And candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final List<Filter> subs = candidate.getChildren();
        sb.append('(');
        for(int i=0,n=subs.size();i<n;i++){
            if(i>0){
                sb.append(" AND ");
            }
            subs.get(i).accept(this, o);
        }
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Id candidate, Object o) {
        throw new IllegalArgumentException("ID filter not supported");
    }

    @Override
    public StringBuilder visit(Not candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        sb.append("NOT(");
        candidate.getFilter().accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Or candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final List<Filter> subs = candidate.getChildren();
        sb.append('(');
        for(int i=0,n=subs.size();i<n;i++){
            if(i>0){
                sb.append(" OR ");
            }
            subs.get(i).accept(this, o);
        }
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(PropertyIsBetween candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final Expression exp = candidate.getExpression();
        final Expression lower = candidate.getLowerBoundary();
        final Expression upper = candidate.getUpperBoundary();

        exp.accept(this, o);
        sb.append(" BETWEEN ");
        lower.accept(this, o);
        sb.append(" AND ");
        upper.accept(this, o);

        return sb;
    }

    @Override
    public StringBuilder visit(PropertyIsEqualTo candidate, Object o) {
        if(candidate.isMatchingCase()){
            return unwrapPath(candidate.getExpression1(), "$ = "+candidate.getExpression2().accept(this, new StringBuilder()), o);
        }else{
            return unwrapPath(candidate.getExpression1(), "$ ilike "+candidate.getExpression2().accept(this, new StringBuilder()), o);
        }
    }

    @Override
    public StringBuilder visit(PropertyIsNotEqualTo candidate, Object o) {
        if(candidate.isMatchingCase()){
            return unwrapPath(candidate.getExpression1(), "$ <> "+candidate.getExpression2().accept(this, new StringBuilder()), o);
        }else{
            return unwrapPath(candidate.getExpression1(), "$ not ilike "+candidate.getExpression2().accept(this, new StringBuilder()), o);
        }
    }

    @Override
    public StringBuilder visit(PropertyIsGreaterThan candidate, Object o) {
        return unwrapPath(candidate.getExpression1(), "$ > "+candidate.getExpression2().accept(this, new StringBuilder()), o);
    }

    @Override
    public StringBuilder visit(PropertyIsGreaterThanOrEqualTo candidate, Object o) {
        return unwrapPath(candidate.getExpression1(), "$ >= "+candidate.getExpression2().accept(this, new StringBuilder()), o);
    }

    @Override
    public StringBuilder visit(PropertyIsLessThan candidate, Object o) {
        return unwrapPath(candidate.getExpression1(), "$ < "+candidate.getExpression2().accept(this, new StringBuilder()), o);
    }

    @Override
    public StringBuilder visit(PropertyIsLessThanOrEqualTo candidate, Object o) {
        return unwrapPath(candidate.getExpression1(), "$ <= "+candidate.getExpression2().accept(this, new StringBuilder()), o);
    }

    @Override
    public StringBuilder visit(PropertyIsLike candidate, Object o) {
        final StringBuilder sb = new StringBuilder();

        final char escape = candidate.getEscape().charAt(0);
        final char wildCard = candidate.getWildCard().charAt(0);
        final char single = candidate.getSingleChar().charAt(0);
        final boolean matchingCase = candidate.isMatchingCase();

        final String literal = candidate.getLiteral();
        String pattern = DefaultPropertyIsLike.convertToSQL92(escape, wildCard, single, literal);

        if(!matchingCase){
            pattern = pattern.toUpperCase();
            sb.append(" UPPER(");
        }

        //we don't know the type, make a type cast to be on the safe side
        sb.append(" CAST( $");
        sb.append(" AS VARCHAR)");

        if(!matchingCase){
            sb.append(")");
        }

        sb.append(" LIKE '");
        sb.append(pattern);
        sb.append("' ");

        final Expression expression = candidate.getExpression();
        return unwrapPath(expression, sb.toString(), o);
    }

    @Override
    public StringBuilder visit(PropertyIsNull candidate, Object o) {
        return unwrapPath(candidate.getExpression(), "$ IS NULL", o);
    }

    @Override
    public StringBuilder visit(PropertyIsNil candidate, Object o) {
        return unwrapPath(candidate.getExpression(), "$ IS NULL", o);
    }

    private StringBuilder unwrapPath(Expression exp, String pattern, Object o){
        return unwrapPath(featureType, exp, pattern, o);
    }

    private StringBuilder unwrapPath(ComplexType type, Expression exp, String pattern, Object o){
        final StringBuilder sb = toStringBuilder(o);

        final StringBuilder subsb = new StringBuilder();
        if(exp instanceof PropertyName){
            final PropertyName prop = ((PropertyName)exp);
            final String path = NamesExt.valueOf(prop.getPropertyName()).tip().toString();

            final int index = path.indexOf('/');
            if(index<0){
                exp.accept(this, subsb);
                sb.append(pattern.replaceFirst("\\$", subsb.toString()));
            }else{
                final String fieldName = path.substring(0, index);
                final PropertyDescriptor desc = type.getDescriptor(fieldName);
                if(desc==null || !(desc.getType() instanceof AssociationType) ){
                    //unknown field, can't make a link
                    exp.accept(this, subsb);
                    sb.append(pattern.replaceFirst("\\$", subsb.toString()));
                }else{

                    final RelationMetaModel relation = (RelationMetaModel)desc.getUserData().get(JDBC_PROPERTY_RELATION);
                    final AssociationType at = (AssociationType) desc.getType();
                    final ComplexType dt = (ComplexType) at.getRelatedType();

                    //sub type filter
                    unwrapPath(dt, new DefaultPropertyName(path.substring(index+1)), pattern, subsb);

                    sb.append(" \"").append(fieldName).append("\"");
                    sb.append(" IN (SELECT \"").append(relation.getForeignColumn()).append('\"');
                    sb.append(" FROM \"").append(relation.getForeignTable());
                    sb.append("\" WHERE ").append(subsb.toString());
                    sb.append(" ) ");
                }
            }

        }else{
            exp.accept(this, subsb);
            sb.append(pattern.replaceFirst("\\$", subsb.toString()));
        }

        return sb;
    }

    @Override
    public StringBuilder visit(BBOX candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        sb.append("st_intersects(");
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Beyond candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);

        if(prepared.swap){
            sb.append("st_dwithin(");
            prepared.property.accept(this, o);
            sb.append(',');
            prepared.geometry.accept(this, o);
            sb.append(',');
            sb.append(candidate.getDistance());
            sb.append(')');
        }else{
            sb.append("st_distance(");
            prepared.property.accept(this, o);
            sb.append(',');
            prepared.geometry.accept(this, o);
            sb.append(") > ");
            sb.append(candidate.getDistance());
        }

        return sb;
    }

    @Override
    public StringBuilder visit(Contains candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        if(prepared.swap){
            sb.append("st_within(");
        }else{
            sb.append("st_contains(");
        }
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Crosses candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        sb.append("st_crosses(");
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Disjoint candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        sb.append("not(st_intersects(");
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append("))");
        return sb;
    }

    @Override
    public StringBuilder visit(DWithin candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);

        if(prepared.swap){
            sb.append("st_distance(");
            prepared.property.accept(this, o);
            sb.append(',');
            prepared.geometry.accept(this, o);
            sb.append(',');
            sb.append(candidate.getDistance());
            sb.append(')');
        }else{
            sb.append("st_dwithin(");
            prepared.property.accept(this, o);
            sb.append(',');
            prepared.geometry.accept(this, o);
            sb.append(") > ");
            sb.append(candidate.getDistance());
        }

        return sb;
    }

    @Override
    public StringBuilder visit(Equals candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        sb.append("st_equals(");
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Intersects candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        sb.append("st_intersects(");
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Overlaps candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        sb.append("st_overlaps(");
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Touches candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        sb.append("st_touches(");
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append(')');
        return sb;
    }

    @Override
    public StringBuilder visit(Within candidate, Object o) {
        final StringBuilder sb = toStringBuilder(o);
        final PreparedSpatialFilter prepared = new PreparedSpatialFilter(candidate);
        if(prepared.swap){
            sb.append("st_contains(");
        }else{
            sb.append("st_within(");
        }
        prepared.property.accept(this, o);
        sb.append(',');
        prepared.geometry.accept(this, o);
        sb.append(')');
        return sb;
    }


    ////////////////////////////////////////////////////////////////////////////
    // TEMPORAL filters are not supported //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public StringBuilder visit(After candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(AnyInteracts candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(Before candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(Begins candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(BegunBy candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(During candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(EndedBy candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(Ends candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(Meets candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(MetBy candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(OverlappedBy candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(TContains candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(TEquals candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    @Override
    public StringBuilder visit(TOverlaps candidate, Object o) {
        throw new UnsupportedOperationException("Temporal filters not supported.");
    }

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS /////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////


    private static StringBuilder toStringBuilder(Object candidate){
        if(candidate instanceof StringBuilder){
            return (StringBuilder) candidate;
        }else{
            throw new RuntimeException("Expected a StringBuilder argument");
        }
    }

    /**
     * Ensure the given double is not an infinite, doesn't work well with SQL and H2.
     * @param candidate
     * @return double unchanged if not an infinite.
     */
    private static double checkInfinites(final double candidate){
        if(candidate == Double.NEGATIVE_INFINITY){
            return Double.MIN_VALUE;
        }else if(candidate == Double.POSITIVE_INFINITY){
            return Double.MAX_VALUE;
        }else{
            return candidate;
        }
    }

    /**
     * prepare a spatial filter, isolate the field and geometry parts.
     * Enventually converting it in a geometry.
     */
    private class PreparedSpatialFilter{

        public PropertyName property;
        public Literal geometry;
        public boolean swap;

        public PreparedSpatialFilter(final BinarySpatialOperator filter){
            final Expression exp1 = filter.getExpression1();
            final Expression exp2 = filter.getExpression2();

            if(exp1 instanceof PropertyName){
                swap = false;
                property = (PropertyName)exp1;
                geometry = (Literal)exp2;
            }else{
                swap = true;
                property = (PropertyName)exp2;
                geometry = (Literal)exp1;
            }

            //change Envelope in polygon
            final Object obj = geometry.getValue();
            if (obj instanceof Envelope) {
                final Envelope env = (Envelope) obj;
                final FilterFactory ff = FactoryFinder.getFilterFactory(null);
                final GeometryFactory gf = new GeometryFactory();
                final Coordinate[] coords = new Coordinate[5];
                double minx = checkInfinites(env.getMinimum(0));
                double maxx = checkInfinites(env.getMaximum(0));
                double miny = checkInfinites(env.getMinimum(1));
                double maxy = checkInfinites(env.getMaximum(1));

                coords[0] = new Coordinate(minx,miny);
                coords[1] = new Coordinate(minx,maxy);
                coords[2] = new Coordinate(maxx,maxy);
                coords[3] = new Coordinate(maxx,miny);
                coords[4] = new Coordinate(minx,miny);
                final LinearRing ring = gf.createLinearRing(coords);
                final Geometry geom = gf.createPolygon(ring, new LinearRing[0]);
                geometry = ff.literal(geom);
            }

            //set the current srid, extract it from feature type
            //requiered when encoding geometry
            currentsrid = -1;
            if (featureType != null) {
                final AttributeDescriptor descriptor = (AttributeDescriptor) property.evaluate(featureType);
                if (descriptor instanceof GeometryDescriptor) {
                    Integer srid = (Integer) descriptor.getUserData().get(JDBCFeatureStore.JDBC_PROPERTY_SRID);
                    if(srid!=null){
                        currentsrid = srid;
                    }
                }
            }

        }

    }

}
