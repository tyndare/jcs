package com.vividsolutions.jcs.qa.diff;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.geom.*;
import com.vividsolutions.jcs.feature.FeatureCollectionGeometryIterator;
import com.vividsolutions.jcs.algorithm.VertexHausdorffDistance;

public class UniqueSegmentsWithToleranceFinder {

  public static double maximumDistance(LineSegment seg1, LineSegment seg2)
  {
    double maxDist = 0.0;
    double dist;
    dist = seg1.p0.distance(seg2.p0);
    maxDist = dist;

    dist = seg1.p0.distance(seg2.p1);
    if (dist > maxDist) maxDist = dist;

    dist = seg1.p1.distance(seg2.p0);
    if (dist > maxDist) maxDist = dist;

    dist = seg1.p1.distance(seg2.p1);
    if (dist > maxDist) maxDist = dist;

    return maxDist;
  }

  private FeatureCollection queryFC;
  private SegmentIndex segIndex;
  private List resultSegs = new ArrayList();
  private Envelope queryEnv = new Envelope();

  public UniqueSegmentsWithToleranceFinder(FeatureCollection fc0, FeatureCollection fc1)
  {
    queryFC = fc0;
    segIndex = new SegmentIndex(fc1);
  }

  public List findUniqueSegments(double tolerance)
  {
    for (Iterator it = new FeatureCollectionGeometryIterator(queryFC); it.hasNext(); ) {
      Geometry geom = (Geometry) it.next();
      findUniqueSegments(geom, tolerance);
    }
    return resultSegs;
  }

  public void findUniqueSegments(Geometry geom, double tolerance)
  {
    List coordArrays = CoordinateArrays.toCoordinateArrays(geom, false);
    for (Iterator i = coordArrays.iterator(); i.hasNext(); ) {
      findUniqueSegments((Coordinate[]) i.next(), tolerance);
    }
  }
  public void findUniqueSegments(Coordinate[] coord, double tolerance)
  {
    for (int i = 0; i < coord.length - 1; i++) {
      LineSegment querySeg = new LineSegment(coord[i], coord[i + 1]);
      querySeg.normalize();
      queryEnv.init(querySeg.p0, querySeg.p1);
      Envelope queryEnvExp = EnvelopeUtil.expand(queryEnv, tolerance);
      List testSegs = segIndex.query(queryEnvExp);
      if (! hasSegmentWithinTolerance(querySeg, testSegs, tolerance))
        resultSegs.add(querySeg);
    }
  }

  private boolean hasSegmentWithinTolerance(LineSegment querySeg, List testSegs, double tolerance)
  {
    for (Iterator i = testSegs.iterator(); i.hasNext(); ) {
      LineSegment testSeg = (LineSegment) i.next();
      VertexHausdorffDistance vhd = new VertexHausdorffDistance(querySeg, testSeg);
      if (vhd.distance() < tolerance)
        return true;
      //if (maximumDistance(querySeg, testSeg) < tolerance)
      //  return true;
    }
    return false;
  }

}